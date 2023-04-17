package com.homero.homeroimagearranger.screen

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.homero.homeroimagearranger.PdfGenerator
import java.io.IOException

@Composable
fun MainScreen() {
    ImageSelector()
}

/* Store all the uris of the selected images */
var lastUri = mutableListOf<Uri>()

@Composable
fun ImageSelector(){
    /* Save states for images and uris */
    var selectImages by remember { mutableStateOf(listOf<Uri>()) }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            selectImages = it
        }
    val context = LocalContext.current

    /* Store the metadata of a single image */
    var text by remember { mutableStateOf("Click on any image to see metadata") }


    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { lastUri.clear(); galleryLauncher.launch("image/*") },
            modifier = Modifier
                .wrapContentSize()
                .padding(10.dp)
        ) {
            Text(text = "Select Images")
        }

        /* Create the grid to place all the images */

        LazyVerticalGrid(columns = GridCells.Fixed(3),Modifier.height(550.dp)) {
            items(selectImages) { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp, 8.dp)
                        .size(100.dp)
                        .clickable {
                            val metaData = getDropboxIMGSize(context, uri)
                            Log.d("Tag", "$metaData")
                            text = metaData.toString()
                        }
                )

                /* Save the uri for each individual image */
                lastUri.add(uri)

            }
        }

        Text(text)

        /* Call the PDFGenerator file */
        PdfGenerator()

    }
}

fun getDropboxIMGSize(context : Context, uri: Uri) : List<String?>{
    /* Returns the image time,width and length*/
    var returnArray = listOf<String?>()
    try {
        context.contentResolver.openInputStream(uri).use { inputStream ->
            val exif = inputStream?.let { ExifInterface(it) }
            val imageTime = exif?.getAttribute(ExifInterface.TAG_DATETIME)
            val imageWidth = exif?.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
            val imageLength = exif?.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
            returnArray = listOf(imageTime,imageWidth,imageLength)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return (returnArray)
}


