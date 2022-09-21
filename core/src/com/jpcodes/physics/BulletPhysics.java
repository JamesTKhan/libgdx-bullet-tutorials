package com.jpcodes.physics;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.jpcodes.physics.screens.BasicCollisionDetection;
import com.jpcodes.physics.screens.BoundingBoxCollision;
import com.jpcodes.physics.screens.SelectScreen;

public class BulletPhysics extends Game {
	
	@Override
	public void create () {
		Bullet.init();
		setScreen(new SelectScreen(this));
	}
}
