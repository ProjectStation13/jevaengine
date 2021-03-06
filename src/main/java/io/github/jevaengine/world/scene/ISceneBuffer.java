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
package io.github.jevaengine.world.scene;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.NullGraphic;
import io.github.jevaengine.math.Matrix3X3;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelComponent;

import java.awt.*;
import java.util.Collection;

public interface ISceneBuffer extends IImmutableSceneBuffer {
	void addModel(IImmutableSceneModel model, @Nullable IEntity dispatcher, Vector3F location);

	void addModel(IImmutableSceneModel model, Vector3F location);

	void addEffect(ISceneBufferEffect effect);

	void reset();

	void translate(Vector2D translation);

	public interface ISceneBufferEffect {
		default IRenderable getUnderlay(Vector2D translation, Rect2D bounds, Matrix3X3 projection) {
			return new NullGraphic();
		}

		default IRenderable getOverlay(Vector2D translation, Rect2D bounds, Matrix3X3 projection) {
			return new NullGraphic();
		}

		ISceneComponentEffect[] getComponentEffect(Graphics2D g, int offsetX, int offsetY, float scale, Vector2D renderLocation, Matrix3X3 projection, ISceneBufferEntry subject, Collection<ISceneBufferEntry> beneath);
	}

	public interface ISceneComponentEffect {
		public void prerender();

		public void postrender();

		boolean ignore(@Nullable IEntity dispatcher, ISceneModelComponent c);
	}

	public interface ISceneBufferEntry {
		@Nullable
		IEntity getDispatcher();

		ISceneModelComponent getComponent();

		Rect2D getProjectedAABB();
	}

	public static final class NullComponentEffect implements ISceneComponentEffect {
		@Override
		public void prerender() {
		}

		@Override
		public void postrender() {
		}

		@Override
		public boolean ignore(IEntity dispatcher, ISceneModelComponent c) {
			return false;
		}
	}
}
