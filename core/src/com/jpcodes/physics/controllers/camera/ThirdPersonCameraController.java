package com.jpcodes.physics.controllers.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.jpcodes.physics.utils.Utils3D;

/**
 * Simple Third Person Camera to follow a given Model Instance
 *
 * @author JamesTKhan
 * @version October 04, 2022
 */
public class ThirdPersonCameraController extends CameraController {
    private final Vector3 position = new Vector3();
    private final Vector3 newPosition = new Vector3();
    private final Vector3 direction = new Vector3();

    private final Camera camera;
    private ModelInstance followTarget;

    private float interpolationSpeed = 2f;
    private float heightAdjustment = 5f;
    private float cameraDistance = 8f;

    public ThirdPersonCameraController(Camera camera, ModelInstance followTarget) {
        this.camera = camera;
        this.followTarget = followTarget;
    }

    @Override
    public void update(float delta) {
        //Get direction and position
        Utils3D.getDirection(followTarget.transform, direction);
        Utils3D.getPosition(followTarget.transform, position);

        newPosition.set(position);

        // Interpolate cam position based on follow targets current direction
        camera.position.lerp(newPosition.add(direction.scl(-cameraDistance)), interpolationSpeed * delta);

        // Interpolate camera height
        if (heightAdjustment != 0)
            camera.position.y = MathUtils.lerp(camera.position.y, camera.position.y + heightAdjustment, interpolationSpeed * delta);

        // Look at the follow targets current position
        camera.lookAt(position);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    public void setHeightAdjustment(float heightAdjustment) {
        this.heightAdjustment = heightAdjustment;
    }

    public void setInterpolationSpeed(float interpolationSpeed) {
        this.interpolationSpeed = interpolationSpeed;
    }

    public void setFollowTarget(ModelInstance followTarget) {
        this.followTarget = followTarget;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        cameraDistance += amountY;
        return true;
    }
}
