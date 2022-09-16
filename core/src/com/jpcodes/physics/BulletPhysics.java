package com.jpcodes.physics;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.jpcodes.physics.screens.BasicCollisionDetection;

public class BulletPhysics extends Game {
	
	@Override
	public void create () {
		Bullet.init();
		setScreen(new BasicCollisionDetection(this));
	}
}
