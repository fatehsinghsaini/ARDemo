package edu.com.os.arDemo

import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {
    private var fragment: CustomArFragment? = null
    private var cloudAnchor: Anchor? = null

    private enum class AppAnchorState {
        NONE, HOSTING, HOSTED, RESOLVING, RESOLVED
    }

    private var appAnchorState = AppAnchorState.NONE
    private val snackbarHelper = SnackbarHelper()
    private val selectedObject: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment?
        fragment!!.planeDiscoveryController.hide() // Hide initial hand gesture
        fragment!!.arSceneView.scene.addOnUpdateListener { frameTime: FrameTime -> onUpdateFrame(frameTime) }

        //InitializeGallery();
        val clearButton = findViewById<Button>(R.id.clear_button)
        clearButton.setOnClickListener { setCloudAnchor(null) }
        val resolveButton = findViewById<Button>(R.id.resolve_button)
        resolveButton.setOnClickListener(View.OnClickListener {
            if (cloudAnchor != null) {
                snackbarHelper.showMessageWithDismiss(parent, "Please clear anchor.")
                return@OnClickListener
            }
            val dialog = ResolveDialogFragment()
            dialog.setOkListener { dialogValue: String -> onResolveOKPressed(dialogValue) }
            dialog.show(supportFragmentManager, "Resolve")
        })
        fragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent? ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING ||
                    appAnchorState != AppAnchorState.NONE) {
                return@setOnTapArPlaneListener
            }
            val newAnchor = fragment!!.arSceneView.session!!.hostCloudAnchor(hitResult.createAnchor())
            setCloudAnchor(newAnchor)
            appAnchorState = AppAnchorState.HOSTING
            snackbarHelper.showMessage(this, "Hosting Anchor...")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                placeObject(fragment, cloudAnchor, selectedObject)
            }
        }

        // storageManager = new StorageManager(this);
    }

    private fun onResolveOKPressed(dialogValue: String) {

    }

    private fun setCloudAnchor(newAnchor: Anchor?) {
        if (cloudAnchor != null) {
            cloudAnchor!!.detach()
        }
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
        snackbarHelper.hide(this)
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        // checkUpdatedAnchor();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(fragment: ArFragment?, anchor: Anchor?, model: Uri?) {
//       val url= "http://13.233.115.47/backend/public/img/chair.sfb"
//       val url= "http://13.233.115.47/backend/public/img/mesa_de_roble_con_vidrio_-_minecraft/scene.gltf"
//       val url= "http://13.233.115.47/backend/public/img/obj_gltf1.glb"
//       val url= "http://13.233.115.47/backend/public/img/cottage_obj.glb"
      val url= "http://13.233.115.47/backend/public/img/1564064431190-9-Spruce-Red.glb"
//       val url= "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf"
        try {

            ModelRenderable.builder()
                    .setSource(this, RenderableSource.builder().setSource(
                            this,
                            Uri.parse(url),
                            RenderableSource.SourceType.GLB).setRecenterMode(RenderableSource.RecenterMode.ROOT).build()).setRegistryId(url)
                    .build()
                    .thenAccept { renderable: ModelRenderable -> addNodeToScene(fragment, anchor, renderable) }
                    .exceptionally { throwable: Throwable ->
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setMessage(throwable.message)
                                .setTitle("Error!")
                        val dialog = builder.create()
                        dialog.show()
                        null
                    }


        } catch (e: Exception) {
        }



    }

    // Place the object on AR
    private fun addNodeToScene(fragment: ArFragment?, anchor: Anchor?, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment!!.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

}