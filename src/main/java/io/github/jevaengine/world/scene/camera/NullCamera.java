/* 
 * Copyright (C) 2015 Jeremy Wildsmith.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.jevaengine.world.scene.camera;

import io.github.jevaengine.world.scene.camera.ICamera;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.scene.IImmutableSceneBuffer;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.scene.ISceneBuffer.ISceneBufferEffect;
import io.github.jevaengine.world.scene.NullSceneBuffer;

public final class NullCamera implements ICamera
{
	@Override
	public IImmutableSceneBuffer getScene(Rect2D bounds, float scale)
	{
		return new NullSceneBuffer();
	}
	
	@Override
	public void addEffect(ISceneBufferEffect e) { }
	
	@Override
	public void removeEffect(ISceneBufferEffect e) { }

	@Override
	public void dettach() { }

	@Override
	public void attach(World world) { }

	@Override
	public Vector3F getLookAt()
	{
		return new Vector3F();
	}
}