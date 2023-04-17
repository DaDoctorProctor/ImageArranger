package com.homero.homeroimagearranger

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.homero.homeroimagearranger.screen.lastUri
import java.io.File
import java.io.FileOutputStream


@Composable
fun PdfGenerator() {
    /* Composable exclusives */
    val ctx = LocalContext.current
    val contentResolver: ContentResolver = LocalContext.current.contentResolver

    /* Create the generate the Generate PDF button */
    Button(
        modifier = Modifier
            .padding(20.dp),
        onClick = {
            generatePDF(ctx, contentResolver)
        }) {
        Text(modifier = Modifier.padding(6.dp), text = "Generate PDF")
    }

}


fun generatePDF(context: Context, contentResolver : ContentResolver) {
    /* Set the PDF page height and width */
    val pageHeight = 792*2//1120
    val pageWidth = 612*2///792

    /* Create the PDF and image object */
    lateinit var resizedImage: Bitmap
    val pdfDocument = PdfDocument()
    val paint = Paint()

    //val bmp1: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(lastUri.last().toString()))
    //val bmp: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.a)

    /* Rotation matrix*/
    val matrix = Matrix()
    matrix.postRotate(90.0f)
    var newHeight = 0F
    var newWidth = 0F


    /* Page creation */
    val myPageInfo: PdfDocument.PageInfo? = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)
    val canvas: Canvas = myPage.canvas

    /* LEGACY PYTHON: Variables */
    val fileList = lastUri
    val numberOfImages =  lastUri.size
    val rows = getMatrixDimensions(numberOfImages).first
    val columns = getMatrixDimensions(numberOfImages).second
    val imageResultRows = 792*2F//792*2
    val imageResultColumns = 612*2F//612*2
    val incrementY = (imageResultRows/rows.toFloat())
    val incrementX = (imageResultColumns/columns.toFloat())
    var fixResolutionToHeight = true
    var initialRow = 0F
    var imageIndex = 0F
    Log.d("Increment","X: $incrementX Y: $incrementY")

    /* LEGACY PYTHON: Image algorithm */
    for (i in 0 until rows) {
        var initialColumn = 0F
        for (j in 0 until columns) {
            /* start if */
            if (imageIndex < numberOfImages) {
                val file = fileList[imageIndex.toInt()]
                var photo: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(file.toString()))
                var width = photo.width
                var height = photo.height
                Log.d("PreWidth" , "$width $height")
                if (width > height){ /* if rotate */
                    if (incrementX > incrementY){
                        fixResolutionToHeight = true
                    } else {
                        photo = Bitmap.createBitmap(photo, 0, 0, width, height, matrix, true)
                        if (rows > columns){
                            fixResolutionToHeight = false
                        } else {
                            fixResolutionToHeight = true
                        }

                    }
                } else {
                    if (incrementY < incrementX){
                        photo = Bitmap.createBitmap(photo, 0, 0, width, height, matrix, true)
                        if (rows > columns){
                            fixResolutionToHeight = true
                        } else {
                            fixResolutionToHeight = false
                        }
                    } else {
                        fixResolutionToHeight = true
                    }
                } /* end if rotate */
                width = photo.width
                height = photo.height
                val widthFloat = width.toFloat()
                val heightFloat = height.toFloat()
                Log.d("PosWidth" , "$width $height")
                val ratio = (widthFloat/heightFloat)
                Log.d("Ratio" , "$ratio")
                if (fixResolutionToHeight){
                    var temporalRows = incrementY
                    /* declared variables before */
                    while (true){
                        newHeight = temporalRows
                        newWidth = (ratio * newHeight)
                        temporalRows-= 10F
                        if (newWidth < incrementX && newHeight < incrementY){
                            break
                        }
                    }
                    Log.d("newWidth", "$newWidth $newHeight")
                    resizedImage = Bitmap.createScaledBitmap(photo,
                        newWidth.toInt(), newHeight.toInt(), false)

                } else {
                    var temporalColumns = incrementX
                    while (true){
                        newWidth = temporalColumns
                        newHeight = (ratio * newWidth)
                        temporalColumns -= 10F
                        if (newWidth < incrementX && newHeight < incrementY){
                            break
                        }
                    }
                    resizedImage = Bitmap.createScaledBitmap(photo,
                        newWidth.toInt(), newHeight.toInt(), false)
                }

                /* Paste the image */
                canvas.drawBitmap(resizedImage, initialColumn, initialRow, paint)
                Log.d("Success", "pasted image at X: $initialColumn Y: $initialRow")
                initialColumn += incrementX
            } /* end if */

            imageIndex += 1
        }

        initialRow += incrementY
    }


    pdfDocument.finishPage(myPage)

    /* Assign the directory and name where the pdf will be saved */
    val directory = "Documents"
    val folderName = "HomeroImageArranger"
    val file = File("${Environment.getExternalStorageDirectory()}/$directory/$folderName", "collage.pdf")

    /* Verify if the PDF was successfully written */
    try {
        pdfDocument.writeTo(FileOutputStream(file))

        Toast.makeText(context, "PDF file generated.. at ", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Fail to generated PDF file..", Toast.LENGTH_SHORT)
            .show()
    }
    pdfDocument.close()
}

fun getMatrixDimensions(argument: Int): Pair<Int, Int> {
    /* LEGACY PYTHON: Matrix switcher; get the dimensions of the image */
    val switcher = mapOf(
        1 to Pair(1, 1),
        2 to Pair(1, 2),
        3 to Pair(1, 3),
        4 to Pair(2, 2),
        5 to Pair(2, 3),
        6 to Pair(3, 2),
        7 to Pair(4, 2),
        8 to Pair(2, 4),
        9 to Pair(3, 3),
        10 to Pair(2, 5),
        11 to Pair(4, 3),
        12 to Pair(4, 3),
        13 to Pair(7, 2),
        14 to Pair(7, 2),
        15 to Pair(5, 3),
        16 to Pair(4, 4),
        17 to Pair(6, 3),
        18 to Pair(6, 3),
        19 to Pair(5, 4),
        20 to Pair(5, 4),
        21 to Pair(7, 3)
    )
    return switcher[argument] ?: Pair(0, 0)
}
