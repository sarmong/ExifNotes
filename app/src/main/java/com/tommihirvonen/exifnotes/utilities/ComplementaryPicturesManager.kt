package com.tommihirvonen.exifnotes.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Chronometer
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import com.tommihirvonen.exifnotes.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manages all complementary pictures attached to frames.
 */
object ComplementaryPicturesManager {

    /**
     * Constant specifying the maximum allowed length of the complementary picture's longer side.
     */
    private const val MAX_SIZE = 1024

    /**
     * Method to get the directory location of complementary pictures.
     *
     * @param context activity's context
     * @return directory File for the location of complementary pictures
     */
    private fun getComplementaryPicturesDirectory(context: Context): File? {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    /**
     * Creates a new placeholder file with a universally unique
     * 128-bit filename in the complementary pictures location.
     *
     * @param context activity's context
     * @return File referencing to the newly created placeholder File
     */
    fun createNewPictureFile(context: Context): File {
        // Create a unique name for the new picture file
        val pictureFilename = UUID.randomUUID().toString() + ".jpg"
        // Create a reference to the picture file
        val picture = getPictureFile(context, pictureFilename)
        // Get reference to the destination folder by the file's parent
        val pictureStorageDirectory = picture.parentFile
        // If the destination folder does not exist, create it
        if (pictureStorageDirectory != null && !pictureStorageDirectory.exists()) {
            pictureStorageDirectory.mkdirs() // also create possible non-existing parent directories -> mkdirs()
        }
        // Return the File
        return picture
    }

    /**
     * Method the get reference to a complementary picture file in the complementary pictures
     * location with only the filename.
     *
     * @param context activity's location
     * @param fileName the name of the complementary picture file
     * @return reference to the complementary picture file
     */
    fun getPictureFile(context: Context, fileName: String): File {
        // Get the absolute path to the picture file.
        return File(getComplementaryPicturesDirectory(context), fileName)
    }

    /**
     * Method to copy a complementary picture to a public external storage directory
     * and to notify the gallery application(s), that they should scan that file.
     *
     * @param context activity's context
     * @param filename the name of the complementary picture
     * @throws IOException thrown if the file copying failed
     */
    fun addPictureToGallery(context: Context, filename: String?) {
        if (filename == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + context.getString(R.string.app_name))
            contentValues.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri = context.contentResolver
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val source = getPictureFile(context, filename)
                val outputStream = context.contentResolver.openOutputStream(uri)
                val bitmap = BitmapFactory.decodeFile(source.absolutePath)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                }
                contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, contentValues, null, null)
            }
        } else {
            @Suppress("DEPRECATION")
            val publicPictureDirectory = File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name))
            val copyFromFile = getPictureFile(context, filename)
            val copyToFile = File(publicPictureDirectory, filename)
            copyFromFile.copyTo(target = copyToFile, overwrite = true)
            @Suppress("DEPRECATION")
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(copyToFile)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }
    }

    /**
     * Compresses a complementary picture file to the size specified by MAX_SIZE.
     *
     * @param context activity's context
     * @param fileName the name of the complementary picture
     * @throws IOException thrown if saving the bitmap causes an IOException
     */
    @Throws(IOException::class)
    fun compressPictureFile(context: Context, fileName: String) {
        // Compress the image
        val pictureFile = getPictureFile(context, fileName)
        if (pictureFile.exists()) {
            val bitmap = getCompressedBitmap(pictureFile)
            pictureFile.delete()
            saveBitmapToFile(bitmap, pictureFile)
        }
    }

    /**
     * Saves bitmap to a file
     *
     * @param bitmap bitmap to be saved
     * @param toFile file where the bitmap should be saved
     * @throws IOException if the file was not found or if the output stream could not be flushed/closed
     */
    @Throws(IOException::class)
    fun saveBitmapToFile(bitmap: Bitmap, toFile: File) {
        val out = FileOutputStream(toFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
    }

    /**
     * Get compressed bitmap from Uri.
     * Method first decodes the bitmap using scaling to save memory
     * and then resizes it to exact dimension.
     *
     * @param context activity's context
     * @param uri uri to the file
     * @return compressed bitmap
     * @throws FileNotFoundException if no file was found using the given Uri
     */
    @Throws(FileNotFoundException::class)
    fun getCompressedBitmap(context: Context, uri: Uri?): Bitmap? {
        // Get the dimensions of the picture
        val options = BitmapFactory.Options()
        // Setting the inJustDecodeBounds property to true while decoding avoids memory allocation,
        // returning null for the bitmap object but setting outWidth, outHeight and outMimeType.
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri!!), null, options)
        options.inSampleSize = calculateInSampleSize(options)
        // Set inJustDecodeBounds back to false, so that decoding returns a bitmap object.
        options.inJustDecodeBounds = false
        // Load the bitmap into memory using the inSampleSize option to reduce the excess resolution
        // and to avoid OutOfMemoryErrors.
        val decodedBitmap = BitmapFactory
                .decodeStream(context.contentResolver.openInputStream(uri), null, options)
        // Then resize the bitmap to the exact dimensions specified by MAX_SIZE.
        return decodedBitmap?.let { getResizedBitmap(it) }
    }

    /**
     * Get compressed bitmap from file.
     * Method first decodes the bitmap using scaling to save memory
     * and then resizes it to exact dimension.
     *
     * @param pictureFile file to decoded to bitmap
     * @return compressed bitmap
     */
    private fun getCompressedBitmap(pictureFile: File): Bitmap {
        // Get the dimensions of the picture
        val options = BitmapFactory.Options()
        // Setting the inJustDecodeBounds property to true while decoding avoids memory allocation,
        // returning null for the bitmap object but setting outWidth, outHeight and outMimeType.
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pictureFile.absolutePath, options)
        options.inSampleSize = calculateInSampleSize(options)
        // Set inJustDecodeBounds back to false, so that decoding returns a bitmap object.
        options.inJustDecodeBounds = false
        // Load the bitmap into memory using the inSampleSize option to reduce the excess resolution
        // and to avoid OutOfMemoryErrors.
        val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath, options)
        // Then resize the bitmap to the exact dimensions specified by MAX_SIZE.
        return getResizedBitmap(bitmap)
    }

    /**
     * Resizes bitmap to exact dimensions and returns a new bitmap object (if resizing was made).
     *
     * @param bitmap the bitmap to be resized
     * @return a new bitmap with exact maximum dimensions
     */
    private fun getResizedBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = MAX_SIZE
        val outWidth: Int
        val outHeight: Int
        val inWidth = bitmap.width
        val inHeight = bitmap.height

        if (maxSize >= maxOf(inHeight, inWidth)) return bitmap
        if (inWidth > inHeight) {
            outWidth = maxSize
            outHeight = inHeight * maxSize / inWidth
        } else {
            outHeight = maxSize
            outWidth = inWidth * maxSize / inHeight
        }
        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false)
    }

    /**
     * Calculates the value of inSampleSize for bitmap decoding.
     *
     * If set to a value > 1, requests the decoder to subsample the original image,
     * returning a smaller image to save memory. The sample size is the number of pixels in either
     * dimension that correspond to a single pixel in the decoded bitmap. For example,
     * inSampleSize == 4 returns an image that is 1/4 the width/height of the original,
     * and 1/16 the number of pixels. Any value <= 1 is treated the same as 1.
     * Note: the decoder uses a final value based on powers of 2,
     * any other value will be rounded down to the nearest power of 2.
     *
     * @param options the BitmapFactory options for the bitmap to be decoded
     * @return scaling value as integer
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val height = options.outHeight
        val width = options.outWidth
        return if (MAX_SIZE >= maxOf(height, width)) 1 else maxOf(height / MAX_SIZE, width / MAX_SIZE)
    }

    /**
     * Set the picture orientation 90 degrees clockwise using ExifInterface.
     *
     * @param context activity's context
     * @param filename the name of the picture file to be rotated
     * @throws IOException if reading/writing exif data caused an exception
     */
    @Throws(IOException::class)
    fun rotatePictureRight(context: Context, filename: String) {
        val pictureFile = getPictureFile(context, filename)
        val exifInterface = ExifInterface(pictureFile.absolutePath)
        val orientation = exifInterface
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var newOrientation = ExifInterface.ORIENTATION_NORMAL
        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED)
            newOrientation = ExifInterface.ORIENTATION_ROTATE_90
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
            newOrientation = ExifInterface.ORIENTATION_ROTATE_180
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            newOrientation = ExifInterface.ORIENTATION_ROTATE_270
        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, newOrientation.toString())
        exifInterface.saveAttributes()
    }

    /**
     * Set the picture orientation 90 degrees counterclockwise using ExifInterface.
     *
     * @param context activity's context
     * @param filename the name of the picture file to be rotated
     * @throws IOException if reading/writing exif data caused an exception
     */
    @Throws(IOException::class)
    fun rotatePictureLeft(context: Context, filename: String) {
        val pictureFile = getPictureFile(context, filename)
        val exifInterface = ExifInterface(pictureFile.absolutePath)
        val orientation = exifInterface
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var newOrientation = ExifInterface.ORIENTATION_NORMAL
        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED)
            newOrientation = ExifInterface.ORIENTATION_ROTATE_270
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            newOrientation = ExifInterface.ORIENTATION_ROTATE_90
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
            newOrientation = ExifInterface.ORIENTATION_ROTATE_180
        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, newOrientation.toString())
        exifInterface.saveAttributes()
    }

    /**
     * Deletes all complementary pictures which are not linked to any frame in the database.
     *
     * @param context activity's context
     */
    fun deleteUnusedPictures(context: Context) {
        // List of all filenames that are being used in the database
        val complementaryPictureFilenames = context.database.allComplementaryPictureFilenames
        // The application private external storage directory, where complementary pictures are stored
        val picturesDirectory = getComplementaryPicturesDirectory(context)
        // Create a FileNameFilter using the filenames
        val filter = FilenameFilter { _: File?, s: String? -> !complementaryPictureFilenames.contains(s) }
        // Delete all files, that are not filtered
        if (picturesDirectory != null) {
            val files = picturesDirectory.listFiles(filter)
            if (files != null) {
                for (pictureFile in files) {
                    pictureFile.delete()
                }
            }
        }
    }

    /**
     * Exports all linked complementary pictures to a zip in the target directory.
     *
     * @param activity activity
     * @param targetFile the directory where the zip file should be saved
     */
    suspend fun exportComplementaryPictures(activity: Activity, targetFile: File): Pair<Boolean, Int> {
        val complementaryPictureFilenames = activity.database.allComplementaryPictureFilenames
        val picturesDirectory = getComplementaryPicturesDirectory(activity)
        if (picturesDirectory == null) {
            Toast.makeText(activity, activity.resources.getString(R.string.ErrorSharedStorageNotAvailable), Toast.LENGTH_SHORT).show()
            return false to 0
        }
        val filter = FilenameFilter { _: File?, s: String? -> complementaryPictureFilenames.contains(s) }
        val files = picturesDirectory.listFiles(filter)
        // If files is empty, no zip file will be created in ZipFileCreatorAsyncTask
        if (files != null) {
            return ZipFileWriter(activity, files, targetFile).export()
        }
        return false to 0
    }

    /**
     * Imports complementary pictures from a zip file and unzips the to the app's
     * private external storage location.
     *
     * @param activity caller's activity
     * @param zipFile the zip file to be imported
     */
    suspend fun importComplementaryPictures(activity: Activity, zipFile: File): Pair<Boolean, Int> {
        val picturesDirectory = getComplementaryPicturesDirectory(activity)
        if (picturesDirectory == null) {
            Toast.makeText(activity, activity.resources.getString(R.string.ErrorSharedStorageNotAvailable), Toast.LENGTH_SHORT).show()
            return false to 0
        }
        return ZipFileReader(activity, zipFile, picturesDirectory).read()
    }

    /**
     * Creates the directory given as parameter if it does not yet exist
     *
     * @param directory the directory to be checked and created if necessary
     */
    private fun directoryChecker(directory: File?) {
        if (!directory!!.isDirectory) {
            directory.mkdirs()
        }
    }

    private class ZipFileWriter(activity: Activity, private val files: Array<File>, private val zipFile: File) {
        companion object {
            /**
             * Limit the buffer memory size while reading and writing buffer into the zip stream
             */
            private const val BUFFER = 10240 // 10KB - good buffer size for disk access
        }

        private val progressBar: ProgressBar
        private val progressTextView: TextView
        private val chronometer: Chronometer
        private val dialog: AlertDialog

        init {
            val builder = AlertDialog.Builder(activity)
            @SuppressLint("InflateParams")
            val view = activity.layoutInflater.inflate(R.layout.dialog_progress, null)
            builder.setView(view)
            progressBar = view.findViewById(R.id.progress_bar)
            val messageTextView = view.findViewById<TextView>(R.id.textview_1)
            messageTextView.setText(R.string.ExportingComplementaryPicturesPleaseWait)
            progressTextView = view.findViewById(R.id.textview_2)
            chronometer = view.findViewById(R.id.elapsed_time)
            progressBar.max = 100
            progressBar.progress = 0
            progressTextView.text = ""
            dialog = builder.create()
            dialog.setCancelable(false)
        }

        suspend fun export(): Pair<Boolean, Int> {
            dialog.show()
            chronometer.start()

            @Suppress("BlockingMethodInNonBlockingContext")
            val result = withContext(Dispatchers.IO) {
                try {
                    // If the files array is empty, return true and end here.
                    if (files.isEmpty()){
                        return@withContext false to 0
                    }
                    var completedEntries = 0
                    // Publish empty progress to tell the interface, that the process has begun.
                    updateProgress(0, files.size)
                    // Set the ZipOutputStream beginning with FileOutputStream then ZipOutputStream.
                    ZipOutputStream(FileOutputStream(zipFile)).use { outputStream ->
                        // byte array where the bytes read from input stream should be stored.
                        val buffer = ByteArray(BUFFER)
                        // Iterate the files from the files array
                        for (file in files) {
                            // Set the BufferedInputStream using FileInputStream
                            BufferedInputStream(FileInputStream(file), BUFFER).use { inputStream ->
                                val entry = ZipEntry(file.name)
                                // Begin writing a new zip file entry.
                                outputStream.putNextEntry(entry)
                                // BufferedInputStream.read() returns the number of bytes read
                                // or -1 if the end of stream was reached.
                                while (true) {
                                    val count = inputStream.read(buffer, 0, BUFFER)
                                    if (count == -1) break
                                    else outputStream.write(buffer, 0, count)
                                }
                            }
                            ++completedEntries
                            updateProgress(completedEntries, files.size)
                        }
                    }
                    return@withContext true to completedEntries
                } catch (e: IOException) {
                    return@withContext false to 0
                }
            }

            chronometer.stop()
            dialog.dismiss()

            return result
        }

        private suspend fun updateProgress(completed: Int, total: Int) {
            withContext(Dispatchers.Main) {
                val progressPercentage = (completed.toFloat() / total.toFloat() * 100f).toInt()
                progressBar.progress = progressPercentage
                val progressText = "$completed/$total"
                progressTextView.text = progressText
            }
        }
    }

    private class ZipFileReader(activity: Activity, private val zipFile: File, private val targetDirectory: File) {
        companion object {
            /**
             * Limit the buffer memory size while reading and writing buffer from the zip stream
             */
            private const val BUFFER = 10240 // 10KB - good buffer size for disk access
        }

        private val progressBar: ProgressBar
        private val progressTextView: TextView
        private val chronometer: Chronometer
        private val dialog: AlertDialog

        init {
            // Show a dialog with progress bar, elapsed time, completed zip entries and total zip entries.
            val builder = AlertDialog.Builder(activity)
            @SuppressLint("InflateParams")
            val view = activity.layoutInflater.inflate(R.layout.dialog_progress, null)
            builder.setView(view)
            progressBar = view.findViewById(R.id.progress_bar)
            val messageTextView = view.findViewById<TextView>(R.id.textview_1)
            messageTextView.setText(R.string.ImportingComplementaryPicturesPleaseWait)
            progressTextView = view.findViewById(R.id.textview_2)
            chronometer = view.findViewById(R.id.elapsed_time)
            progressBar.max = 100
            progressBar.progress = 0
            progressTextView.text = ""
            dialog = builder.create()
            dialog.setCancelable(false)
        }

        suspend fun read(): Pair<Boolean, Int> {
            dialog.show()
            chronometer.start()

            @Suppress("BlockingMethodInNonBlockingContext")
            val result = withContext(Dispatchers.IO) {
                try {
                    val totalEntries = ZipFile(zipFile).size()
                    // If the zip file was empty, end here.
                    if (totalEntries == 0) {
                        return@withContext false to 0
                    }
                    // Publish empty progress to tell the interface, that the process has begun.
                    updateProgress(0, totalEntries)
                    // Create target directory if it does not exists
                    directoryChecker(targetDirectory)
                    var completedEntries = 0
                    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
                        val buffer = ByteArray(BUFFER)
                        while (true) {
                            // nextEntry returns null if the end was reached -> break loop
                            val zipEntry = zipInputStream.nextEntry ?: break
                            if (zipEntry.isDirectory) {
                                directoryChecker(File(targetDirectory, zipEntry.name))
                            } else {
                                // Set the FileOutputStream using File
                                val targetFile = File(targetDirectory, zipEntry.name)
                                if (targetFile.exists()) targetFile.delete()
                                FileOutputStream(targetFile).use { outputStream ->
                                    while (true) {
                                        // ZipInputStream.read() returns the number of bytes read
                                        // or -1 if the end of stream was reached -> break loop.
                                        val count = zipInputStream.read(buffer, 0, BUFFER)
                                        if (count == -1) break
                                        else outputStream.write(buffer, 0, count)
                                    }
                                }
                                zipInputStream.closeEntry()
                                ++completedEntries
                                updateProgress(completedEntries, totalEntries)
                            }
                        }
                    }
                    return@withContext true to completedEntries
                } catch (e: IOException) {
                    return@withContext false to 0
                }
            }
            chronometer.stop()
            dialog.dismiss()

            return result
        }

        private suspend fun updateProgress(completed: Int, total: Int) {
            withContext(Dispatchers.Main) {
                val progressPercentage = (completed.toFloat() / total.toFloat() * 100f).toInt()
                progressBar.progress = progressPercentage
                val progressText = "$completed/$total"
                progressTextView.text = progressText
            }
        }
    }

}