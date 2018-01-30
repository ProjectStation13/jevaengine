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
package io.github.jevaengine.math;

import io.github.jevaengine.config.*;

public final class Vector2F implements ISerializable {
	public static final float TOLERANCE = 0.00000001F;

	public float x;
	public float y;

	public Vector2F(Vector2F v) {
		x = v.x;
		y = v.y;
	}

	public Vector2F(Vector2D v) {
		x = v.x;
		y = v.y;
	}

	public Vector2F(float fX, float fY) {
		x = fX;
		y = fY;
	}

	public Vector2F() {
		this(0, 0);
	}

	public static Vector2F min(Vector2F a, Vector2F b) {
		return (a.getLengthSquared() > b.getLengthSquared() ? b : a);
	}

	public boolean isZero() {
		return Math.abs(x) < TOLERANCE && Math.abs(y) < TOLERANCE;
	}

	public Vector2F rotate(Vector2F origin, float fAngle) {
		Vector2F rotated = new Vector2F(this).difference(origin);

		return Matrix2X2.createRotation(fAngle).dot(rotated).add(origin);
	}

	public Vector2F rotate(float fAngle) {
		return rotate(new Vector2F(), fAngle);
	}

	public Vector2D floor() {
		return new Vector2D((int) Math.floor(x), (int) Math.floor(y));
	}

	public Vector2D ceil() {
		return new Vector2D((int) Math.ceil(x), (int) Math.ceil(y));
	}

	public Vector2D round() {
		return new Vector2D((int) (Math.round(Math.abs(x)) * Math.signum(x)), (int) (Math.round(Math.abs(y)) * Math.signum(y)));
	}

	public Vector2F negative() {
		return new Vector2F(-x, -y);
	}

	public float getAngle() {
		return (float) Math.atan2(y, x);
	}

	public float getLengthSquared() {
		return x * x + y * y;
	}

	public float getLength() {
		return (float) Math.sqrt(getLengthSquared());
	}

	public Vector2F difference(Vector2F a) {
		return new Vector2F(x - a.x, y - a.y);
	}

	public Vector2F difference(Vector2D a) {
		return difference(new Vector2F(a.x, a.y));
	}

	public Vector2F normalize() {
		return this.divide(getLength());
	}

	public Vector2F add(Vector2F a) {
		return new Vector2F(x + a.x, y + a.y);
	}

	public Vector2F add(Vector2D a) {
		return new Vector2F(x + a.x, y + a.y);
	}

	public Vector2F multiply(float fScale) {
		return new Vector2F(x * fScale, y * fScale);
	}

	public Vector2F divide(float f) {
		return new Vector2F(x / f, y / f);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Vector2F) {
			Vector2F vec = (Vector2F) o;

			if (Math.abs(vec.x - x) > TOLERANCE)
				return false;
			else if (Math.abs(vec.y - y) > TOLERANCE)
				return false;
			else
				return true;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Float.floatToIntBits(this.x);
		hash = 41 * hash + Float.floatToIntBits(this.y);
		return hash;
	}

	@Override
	public void serialize(IVariable target) throws ValueSerializationException {
		target.addChild("x").setValue(x);
		target.addChild("y").setValue(y);
	}

	@Override
	public void deserialize(IImmutableVariable source) throws ValueSerializationException {
		try {
			x = source.getChild("x").getValue(Double.class).floatValue();
			y = source.getChild("y").getValue(Double.class).floatValue();
		} catch (NoSuchChildVariableException e) {
			throw new ValueSerializationException(e);
		}
	}
}
