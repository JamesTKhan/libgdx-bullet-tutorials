package com.jpcodes.physics;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.bullet.Bullet;

public class BulletPhysics extends Game {
	
	@Override
	public void create () {
		Bullet.init();
		setScreen(new SelectScreen(this));
	}
}
