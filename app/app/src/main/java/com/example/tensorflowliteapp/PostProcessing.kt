package com.example.tensorflowliteapp

import android.widget.TextView
import com.example.tensorflowliteapp.ml.EfficientdetLite2


class PostProcessing {

    val number : Int = 3

    fun postProccessingInfo(outputs: EfficientdetLite2.Outputs, textView: TextView) {
        outputs.detectionResultList.forEachIndexed { index, detectionResult ->
            val location = detectionResult.locationAsRectF
            val category = detectionResult.categoryAsString
            val score = detectionResult.scoreAsFloat

            textView.text = "Location:top"+location.top+";\nbottom="+location.bottom+";\nright="+location.right+";\nleft="+location.left+";\n category="+category
            //if(location.top){

            //}

        }
        //val classB = SecondFileTest()
        //textView.text = "Location:top"

        //val list = intent.getSerializableExtra("outputs") as ArrayList<String>
        println("hehe")
        //Toast.makeText(MainActivity, list.joinToString(), Toast.LENGTH_SHORT).show()
    }

}



