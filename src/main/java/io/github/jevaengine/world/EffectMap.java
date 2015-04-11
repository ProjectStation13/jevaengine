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
package io.github.jevaengine.world;

import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.search.ISearchFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EffectMap
{
	private final HashMap<Vector2D, TileEffects> m_tileEffects = new HashMap<>();

	public EffectMap() { }

	public EffectMap(EffectMap map)
	{
		for (Map.Entry<Vector2D, TileEffects> effects : map.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey(), effects.getValue());
	}

	public void clear()
	{
		m_tileEffects.clear();
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		if (!m_tileEffects.containsKey(location))
			return new TileEffects();

		return m_tileEffects.get(location);
	}

	public final TileEffects[] getTileEffects(ISearchFilter<TileEffects> filter)
	{
		List<TileEffects> tileEffects = new ArrayList<>();

		Rect2D searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.x + searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.y + searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
				{
					tileEffects.add(effects);
				}
			}
		}

		return tileEffects.toArray(new TileEffects[tileEffects.size()]);
	}

	public final void applyOverlayEffects(ISearchFilter<TileEffects> filter, TileEffects overlay)
	{
		Rect2D searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (filter.shouldInclude(new Vector2F(x, y)) && effects == null)
				{
					m_tileEffects.put(new Vector2D(x, y), overlay);
					effects = overlay;
				}

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
					effects.overlay(overlay);
			}
		}
	}

	public final void applyOverlayEffects(Vector2D location, TileEffects value)
	{
		if (!m_tileEffects.containsKey(location))
			m_tileEffects.put(location, value);
		else
			m_tileEffects.put(location, m_tileEffects.get(location).overlay(value));
	}

	public final void overlay(EffectMap overlay, Vector2D offset)
	{
		for (Map.Entry<Vector2D, TileEffects> effects : overlay.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey().add(offset), effects.getValue());
	}

	public final void overlay(EffectMap overlay)
	{
		overlay(overlay, new Vector2D());
	}

	public static class TileEffects
	{
		private final Set<IEntity> m_obstructions = new HashSet<>();
		
		public TileEffects() { }
		
		public TileEffects(TileEffects effects)
		{
			m_obstructions.addAll(effects.m_obstructions);
		}

		public TileEffects(IEntity ... obstructions)
		{
			m_obstructions.addAll(Arrays.asList(obstructions));
		}

		public boolean isTraversable(IEntity subject)
		{
			for(IEntity e : m_obstructions)
			{
				if(e != subject && e.getBody().collidesWith(subject.getBody()))
					return false;
			}
			
			return true;
		}
		
		public float getSightEffect()
		{
			return 0;
		}
		
		public static TileEffects merge(TileEffects[] tiles)
		{
			TileEffects effect = new TileEffects();

			for (TileEffects tile : tiles)
				effect = effect.overlay(tile);

			return effect;
		}

		public TileEffects overlay(TileEffects overlay)
		{
			TileEffects newEffects = new TileEffects();
			newEffects.m_obstructions.addAll(m_obstructions);
			newEffects.m_obstructions.addAll(overlay.m_obstructions);
			
			return newEffects;
		}
	}
}
