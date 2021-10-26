package com.example.arcoresceneform

import android.content.ContentResolver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.animal_name.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var arFragment: ArFragment? = null
    private var bearRenderable: ModelRenderable ?= null
    private var catRenderable: ModelRenderable ?= null
    private var cowRenderable: ModelRenderable ?= null
    private var dogRenderable: ModelRenderable ?= null
    private var elephantRenderable: ModelRenderable ?= null
    private var ferretRenderable: ModelRenderable ?= null
    private var hippopotamusRenderable: ModelRenderable ?= null
    private var horseRenderable: ModelRenderable ?= null
    private var koalaBearRenderable: ModelRenderable ?= null
    private var lionRenderable: ModelRenderable ?= null
    private var reindeerRenderable: ModelRenderable ?= null
    private var wolverineRenderable: ModelRenderable ?= null

    private var listVariables : MutableList<ModelRenderable?> = mutableListOf(
        bearRenderable,
        catRenderable,
        cowRenderable,
        dogRenderable,
        elephantRenderable,
        ferretRenderable,
        hippopotamusRenderable,
        horseRenderable,
        koalaBearRenderable,
        lionRenderable,
        reindeerRenderable,
        wolverineRenderable
    )

    private var arrayView = mutableListOf(
        R.id.bear,
        R.id.cat,
        R.id.cow,
        R.id.dog,
        R.id.elephant,
        R.id.ferret,
        R.id.hippopotamus,
        R.id.horse,
        R.id.koala_bear,
        R.id.lion,
        R.id.reindeer,
        R.id.wolverine
    )

    private var arrayRaw = mutableListOf(
        R.raw.bear,
        R.raw.cat,
        R.raw.cow,
        R.raw.dog,
        R.raw.elephant,
        R.raw.ferret,
        R.raw.hippopotamus,
        R.raw.horse,
        R.raw.koala_bear,
        R.raw.lion,
        R.raw.reindeer,
        R.raw.wolverine
    )

    private var selected = 0
    private var mUserRequestedInstall = true
    private var mSession: Session?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.scene_form_fragment) as? ArFragment
        setClickListener()
        setUpModel()
        arFragment?.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            val anchorNote = AnchorNode(anchor)
            anchorNote.setParent(arFragment?.arSceneView?.scene)
            createModel(anchorNote, selected)
        }
    }

    private fun setUpModel() {
        arrayRaw.forEachIndexed { index, item ->
            ModelRenderable.builder()
                .setSource(this, item)
                .setIsFilamentGltf(true)
                .setRegistryId(getUriFromRawFile(item))
                .build()
                .thenAccept { renderable -> listVariables[index] = renderable }
                .exceptionally {
                    run { Toast.makeText(this, "Unable to load ${item.toString().split("Renderable").getOrNull(0)} model", Toast.LENGTH_LONG).show() }
                    return@exceptionally null
                }
        }
    }

    private fun getUriFromRawFile(rawResourceId: Int): Uri? {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .path(rawResourceId.toString())
            .build()
    }

    private fun createModel(anchorNote: AnchorNode, selected: Int) {
        val animal = TransformableNode(arFragment?.transformationSystem)
        animal.apply {
            setParent(anchorNote)
            renderable = listVariables[selected]
            select()
//            addName(anchorNote, animal, arrayName[selected])
        }
    }

    private fun addName(anchorNote: AnchorNode, animal: TransformableNode, name: String) {
        var nameAnimal: ViewRenderable? = null
        ViewRenderable.builder()
            .setView(this, R.layout.animal_name)
            .build()
            .thenAccept { renderable -> nameAnimal = renderable }

        val nameView = TransformableNode(arFragment?.transformationSystem)
        nameView.apply {
            localPosition = Vector3(0f, animal.localPosition.y + 0.5f, 0f)
            setParent(anchorNote)
            renderable = nameAnimal
            select()
        }
        text_animal_name.text = name
        text_animal_name.setOnClickListener {
            anchorNote.setParent(null)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!CameraPermissionHelper.hasCameraPermission(this)){
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        mSession = Session(this)
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = false
                        return
                    }
                    null -> return
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(this, "TODO: handle exception $e", Toast.LENGTH_LONG)
                .show()
            return
        } catch (ex: Exception) {
            return  // mSession remains null, since session creation has failed.
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    private fun setClickListener() {
        arrayView.forEach {
            findViewById<ImageView>(it).setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        arrayView.indexOfFirst { it == v?.id }.let {
            selected = it
        }
    }
}
