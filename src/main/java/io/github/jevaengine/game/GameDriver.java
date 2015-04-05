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
package io.github.jevaengine.game;


import com.google.inject.Inject;
import io.github.jevaengine.util.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class GameDriver
{
	private static final int GAMELOOP_PERIOD = 1000 / 30;
	
	private final ScheduledExecutorService m_executor = new ScheduledThreadPoolExecutor(1);
	
	private final IGame m_game;	
	private final IRenderer m_renderer;
	
	@Nullable
	private ScheduledFuture<?> m_gameLoop;
	
	@Inject
	public GameDriver(IGameFactory gameFactory, IRenderer renderer)
	{
		m_game = gameFactory.create();
		m_renderer = renderer;
	}
	
	public void begin()
	{
		if(m_gameLoop != null && !m_gameLoop.isCancelled() && !m_gameLoop.isDone())
			return;
		
		m_gameLoop = m_executor.scheduleAtFixedRate(new GameLoop(), 0, GAMELOOP_PERIOD, TimeUnit.MILLISECONDS);
	}
	
	public void stop()
	{
		if(m_gameLoop == null)
			return;
		
		m_gameLoop.cancel(false);
	}
	
	private class GameLoop implements Runnable
	{
		private long lastTime = System.currentTimeMillis();
		@Override
		public void run()
		{	
			m_game.render(m_renderer);
			m_game.update((int)(System.currentTimeMillis() - lastTime));
			lastTime = System.currentTimeMillis();
		}
	}
}
