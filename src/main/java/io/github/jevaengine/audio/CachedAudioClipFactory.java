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
package io.github.jevaengine.audio;

import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.IAssetStreamFactory.AssetStreamConstructionException;

import javax.inject.Inject;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.ReferenceQueue;
import java.net.URI;
import java.util.ArrayList;

public final class CachedAudioClipFactory implements IAudioClipFactory {
	private final IAssetStreamFactory m_assetFactory;

	private final ClipCleanup m_clipCleanup = new ClipCleanup();
	private final AudioClipCacheController m_cacheController = new AudioClipCacheController();

	@Inject
	public CachedAudioClipFactory(IAssetStreamFactory assetFactory) {
		m_assetFactory = assetFactory;
	}

	@Override
	public IAudioClip create(URI name) throws AudioClipConstructionException {
		try {
			AudioClipCache cache = m_cacheController.getCache(name);
			return new CachedAudioClip(cache, cache.getClip());
		} catch (IOException |
				UnsupportedAudioFileException |
				UnsupportedAudioFormatException |
				LineUnavailableException |
				AssetStreamConstructionException e) {
			throw new AudioClipConstructionException(name, e);
		}
	}

	public final class AudioClipCacheController {
		private ArrayList<AudioClipCache> m_clipCaches = new ArrayList<>();

		private byte[] constructCache(URI name) throws AssetStreamConstructionException, IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			try (InputStream source = m_assetFactory.create(name)) {
				byte[] readBuffer = new byte[2048];

				int length = 0;

				while ((length = source.read(readBuffer, 0, readBuffer.length)) != -1)
					bos.write(readBuffer, 0, length);

				source.close();
			} catch (UnsupportedEncodingException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}

			return bos.toByteArray();
		}

		public synchronized AudioClipCache getCache(URI name) throws AssetStreamConstructionException, IOException {
			synchronized (m_clipCaches) {
				for (AudioClipCache cache : m_clipCaches) {
					if (cache.getName().equals(name))
						return cache;
				}

				// If we get here there is no clip-cache under m_clipName.
				AudioClipCache cache = new AudioClipCache(name, constructCache(name), m_clipCleanup.getCleanupQueue());
				m_clipCaches.add(cache);
				return cache;
			}
		}

		private void cleanupCache() {
			synchronized (m_clipCaches) {
				ArrayList<AudioClipCache> garbageCaches = new ArrayList<>();

				for (AudioClipCache cache : m_clipCaches) {
					cache.cleanupCache();

					if (cache.isEmpty()) {
						cache.dispose();
						garbageCaches.add(cache);
					}
				}

				m_clipCaches.removeAll(garbageCaches);
			}
		}
	}

	private class ClipCleanup implements Runnable {
		private final ReferenceQueue<Clip> m_referenceQueue = new ReferenceQueue<Clip>();

		public ReferenceQueue<Clip> getCleanupQueue() {
			return m_referenceQueue;
		}

		@Override
		public void run() {
			while (true) {
				try {
					m_referenceQueue.remove().get().close();
					m_cacheController.cleanupCache();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
