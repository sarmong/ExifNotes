/*
 * Exif Notes
 * Copyright (C) 2022  Tommi Hirvonen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tommihirvonen.exifnotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.tommihirvonen.exifnotes.R
import com.tommihirvonen.exifnotes.databinding.FragmentRollEditBinding
import com.tommihirvonen.exifnotes.core.entities.Camera
import com.tommihirvonen.exifnotes.core.entities.FilmStock
import com.tommihirvonen.exifnotes.core.entities.Roll
import com.tommihirvonen.exifnotes.data.repositories.FilmStockRepository
import com.tommihirvonen.exifnotes.utilities.*
import com.tommihirvonen.exifnotes.viewmodels.RollEditViewModel
import com.tommihirvonen.exifnotes.viewmodels.RollEditViewModelFactory
import com.tommihirvonen.exifnotes.viewmodels.RollsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dialog to edit Roll's information
 */
@AndroidEntryPoint
class RollEditFragment : Fragment() {

    @Inject lateinit var filmStockRepository: FilmStockRepository

    val arguments by navArgs<RollEditFragmentArgs>()
    private val rollsModel by activityViewModels<RollsViewModel>()
    private val roll by lazy {
        arguments.roll?: Roll()
    }
    private val model by viewModels<RollEditViewModel> {
        RollEditViewModelFactory(requireActivity().application, filmStockRepository, roll.copy())
    }

    private lateinit var binding: FragmentRollEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                rollsModel.cameras.collect { cameras ->
                    model.cameras = cameras
                }
            }
        }
        sharedElementEnterTransition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeImageTransform())
            .addTransition(Fade())
            .setCommonInterpolator(FastOutSlowInInterpolator())
            .apply { duration = 250L }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRollEditBinding.inflate(inflater, container, false)
        binding.viewmodel = model.observable
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        // Start the transition once all views have been measured and laid out.
        (view.parent as ViewGroup).doOnPreDraw {
            startPostponedEnterTransition()
        }
        observeThenClearNavigationResult<Camera>(ExtraKeys.CAMERA) { camera ->
            rollsModel.addCamera(camera)
            model.observable.setCamera(camera)
        }
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.roll_edit_dest)
        navBackStackEntry.observeThenClearNavigationResult<FilmStock>(viewLifecycleOwner, ExtraKeys.FILM_STOCK) { filmStock ->
            filmStock?.let(model::addFilmStock)
        }
        navBackStackEntry.observeThenClearNavigationResult<FilmStock>(viewLifecycleOwner, ExtraKeys.SELECT_FILM_STOCK) { filmStock ->
            filmStock?.let(model.observable::setFilmStock)
        }
    }

    val dateTimeLoadedHandler by lazy { dateTimePickHandler(model.roll::date, model.observable::setLoadedOn) }

    val dateTimeUnloadedHandler by lazy { dateTimePickHandler(model.roll::unloaded, model.observable::setUnloadedOn) }

    val dateTimeDevelopedHandler by lazy { dateTimePickHandler(model.roll::developed, model.observable::setDevelopedOn) }

    fun openAddFilmStockDialog() {
        binding.noteEditText.clearFocus()
        binding.nameEditText.clearFocus()
        val title = resources.getString(R.string.AddNewFilmStock)
        val positiveButtonText = resources.getString(R.string.Add)
        val action = RollEditFragmentDirections.rollFilmStockEditAction(null, title, positiveButtonText)
        findNavController().navigate(action)
    }

    fun openSelectFilmStockDialog() {
        val action = RollEditFragmentDirections.rollSelectFilmStockAction()
        findNavController().navigate(action)
    }

    fun navigateCameraAddFragment() {
        val title = resources.getString(R.string.AddNewCamera)
        val sharedElement = binding.addCamera
        val action = RollEditFragmentDirections
            .rollCameraEditAction(null, title, sharedElement.transitionName)
        val extras = FragmentNavigatorExtras(
            sharedElement to sharedElement.transitionName
        )
        findNavController().navigate(action, extras)
    }

    fun submit() {
        if (model.validate()) {
            setNavigationResult(model.roll, ExtraKeys.ROLL)
            navigateBack()
        }
    }

    fun navigateBack() = findNavController().navigateUp()
}