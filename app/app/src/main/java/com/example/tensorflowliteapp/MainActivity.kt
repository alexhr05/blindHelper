package com.example.tensorflowliteapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tensorflowliteapp.ml.EfficientdetLite0
import com.example.tensorflowliteapp.ml.EfficientdetLite1
import com.example.tensorflowliteapp.ml.EfficientdetLite2
import com.example.tensorflowliteapp.ml.Mobilenetv1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {
    val paint = Paint()
    lateinit var labels:List<String>
    lateinit var imageProcessor: ImageProcessor
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var textureView:TextureView
    lateinit var cameraManager:CameraManager
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    lateinit var model0: EfficientdetLite0
    lateinit var model1: EfficientdetLite1
    lateinit var model2: EfficientdetLite2
    lateinit var model: Mobilenetv1
    lateinit var number : Number
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = (Handler(handlerThread.looper))
        textView = findViewById(R.id.textViewText)

        imageView = findViewById(R.id.imageView)

        getPermission()

        labels = FileUtil.loadLabels(this,"labels.txt")
        model0 = EfficientdetLite0.newInstance(this)
        model1 = EfficientdetLite1.newInstance(this)
        model2 = EfficientdetLite2.newInstance(this)
        model = Mobilenetv1.newInstance(this)
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300,300, ResizeOp.ResizeMethod.BILINEAR)).build()
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

                bitmap = textureView.bitmap!!

                var image = TensorImage.fromBitmap(bitmap)
                image = imageProcessor.process(image)

                val outputs = model2.process(image)
                val locations = outputs.locationAsTensorBuffer.floatArray
//                val detectionResult = outputs.detectionResultList.get(0)
//                val location = detectionResult.locationAsRectF
//                val category = detectionResult.categoryAsString
//                val score = detectionResult.scoreAsFloat

                var mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true)

                val canvas = Canvas(mutableBitmap)

                val bitmapHeight = 1
                val bitmapWidth = 1

                paint.textSize = bitmapHeight/15f
                paint.strokeWidth = bitmapWidth/85f

                var idx = 0
                val threshold = 0.1
                var postProcessingObj = PostProcessing();
                outputs.detectionResultList.forEachIndexed { index, detectionResult ->
                    idx = index*4
                    val location = detectionResult.locationAsRectF
                    val category = detectionResult.categoryAsString
                    val score = detectionResult.scoreAsFloat


                    if (score >= threshold){

                        Log.d("LOCATION",location.left.toString())
                        Log.d("LOCATION",location.right.toString())
                        Log.d("LOCATION",location.top.toString())
                        Log.d("LOCATION",location.bottom.toString())
                        Log.d("CATEGORY",category)
                        Log.d("SCORE",score.toString())
//                        paint.setColor(Color.BLUE)
//                        paint.style = Paint.Style.STROKE
//                        canvas.drawRect(RectF(locations.get(idx+1)*bitmapWidth, locations.get(idx)*bitmapHeight, locations.get(idx+3)*bitmapWidth, locations.get(idx+2)*bitmapHeight),paint)
//                        paint.style = Paint.Style.FILL
//                        canvas.drawText(category,location.left*bitmapWidth, location.top*bitmapHeight,paint)

                    }

                }
                postProcessingObj.postProccessingInfo(outputs,textView);


            }
        }


        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager


    }

    @SuppressLint("MissingPermission")
    fun openCamera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0],object:CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                var surfaceTexture = textureView.surfaceTexture
                var surface = Surface(surfaceTexture)

                var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(captureRequest.build(),null,null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        TODO("Not yet implemented")
                    }
                }, handler)
            }

            override fun onDisconnected(camera: CameraDevice) {
                TODO("Not yet implemented")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                TODO("Not yet implemented")
            }
        },handler)
    }

    fun getPermission(){
        val cameraPermision = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this,cameraPermision) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(cameraPermision),101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }
}