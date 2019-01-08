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
package io.github.jevaengine.ui;

import io.github.jevaengine.config.*;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.util.Nullable;

import javax.inject.Inject;
import java.net.URI;

public class DefaultControlFactory implements IControlFactory {
	private final IConfigurationFactory m_configurationFactory;

	@Inject
	public DefaultControlFactory(IConfigurationFactory configurationFactory) {
		m_configurationFactory = configurationFactory;
	}

	@Override
	@Nullable
	public Class<? extends Control> lookup(String className) {
		if (className.equals(Button.COMPONENT_NAME))
			return Button.class;
		else if (className.equals(Label.COMPONENT_NAME))
			return Label.class;
		else if (className.equals(TextArea.COMPONENT_NAME))
			return TextArea.class;
		else if (className.equals(Viewport.COMPONENT_NAME))
			return Viewport.class;
		else if (className.equals(Panel.COMPONENT_NAME))
			return Panel.class;
		else if (className.equals(WorldView.COMPONENT_NAME))
			return WorldView.class;
		else if (className.equals(ValueGuage.COMPONENT_NAME))
			return ValueGuage.class;
		else if (className.equals(Checkbox.COMPONENT_NAME))
			return Checkbox.class;

		return null;
	}

	@Override
	public <T extends Control> String lookup(Class<T> controlClass) {
		if (controlClass.equals(Button.class))
			return Button.COMPONENT_NAME;
		else if (controlClass.equals(Label.class))
			return Label.COMPONENT_NAME;
		else if (controlClass.equals(TextArea.class))
			return TextArea.COMPONENT_NAME;
		else if (controlClass.equals(Viewport.class))
			return Viewport.COMPONENT_NAME;
		else if (controlClass.equals(Panel.class))
			return Panel.COMPONENT_NAME;
		else if (controlClass.equals(WorldView.class))
			return WorldView.COMPONENT_NAME;
		else if (controlClass.equals(ValueGuage.class))
			return ValueGuage.COMPONENT_NAME;
		else if (controlClass.equals(Checkbox.class))
			return Checkbox.COMPONENT_NAME;

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Control> T create(Class<T> controlClass, String instanceName, URI config, IImmutableVariable auxConfig) throws ControlConstructionException {
		try {

			String configPath = config.getPath();
			IImmutableVariable configVar = new ImmutableVariableOverlay(auxConfig, configPath.isEmpty() || configPath.endsWith("/") ? new NullVariable() : m_configurationFactory.create(config));

			if (controlClass.equals(Button.class)) {
				String text = configVar.getChild("text").getValue(String.class);
				return (T) new Button(instanceName, text);
			} else if (controlClass.equals(Label.class)) {
				String text = configVar.childExists("text") ? configVar.getChild("text").getValue(String.class) : "";
				return (T) new Label(instanceName, text);
			} else if (controlClass.equals(TextArea.class)) {
				String defaultText = configVar.childExists("text") ? configVar.getChild("text").getValue(String.class) : "";
				boolean editable = configVar.childExists("allowEdit") ? configVar.getChild("allowEdit").getValue(Boolean.class) : true;
				boolean wordWrap = configVar.childExists("wordWrap") ? configVar.getChild("wordWrap").getValue(Boolean.class) : true;
				boolean multiline = configVar.childExists("multiline") ? configVar.getChild("multiline").getValue(Boolean.class) : true;
				int maxLength = configVar.childExists("maxLength") ? configVar.getChild("maxLength").getValue(Integer.class) : -1;
				Rect2D bounds = configVar.getChild("bounds").getValue(Rect2D.class);

				TextArea textArea = new TextArea(instanceName, defaultText, bounds.width, bounds.height);
				textArea.setEditable(editable);
				textArea.setWordWrapped(wordWrap);
				textArea.setMultiline(multiline);
				textArea.setMaxLength(maxLength);

				return (T) textArea;
			} else if (controlClass.equals(Viewport.class)) {
				Rect2D bounds = configVar.getChild("bounds").getValue(Rect2D.class);
				Viewport viewport = new Viewport(instanceName, bounds.width, bounds.height);

				return (T) viewport;
			} else if (controlClass.equals(Panel.class)) {
				Rect2D bounds = configVar.getChild("bounds").getValue(Rect2D.class);
				Panel panel = new Panel(instanceName, bounds.width, bounds.height);

				return (T) panel;
			} else if (controlClass.equals(WorldView.class)) {
				Rect2D bounds = configVar.getChild("bounds").getValue(Rect2D.class);
				WorldView worldView = new WorldView(instanceName, bounds.width, bounds.height);

				return (T) worldView;
			} else if (controlClass.equals(ValueGuage.class)) {
				Rect2D bounds = configVar.getChild("bounds").getValue(Rect2D.class);

				ValueGuage valueGuage = new ValueGuage(instanceName, bounds);

				return (T) valueGuage;
			} else if (controlClass.equals(Checkbox.class)) {
				boolean value = configVar.childExists("value") ? configVar.getChild("value").getValue(Boolean.class) : false;

				return (T) new Checkbox(instanceName, value);
			}
		} catch (ConfigurationConstructionException | ValueSerializationException | NoSuchChildVariableException e) {
			throw new ControlConstructionException(controlClass.getName(), e);
		}

		throw new ControlConstructionException(controlClass.getName(), new UnsupportedControlException());
	}

	@Override
	public Control create(String controlName, String instanceName, URI config, IImmutableVariable auxConfig) throws ControlConstructionException {
		Class<? extends Control> ctrlClass = lookup(controlName);

		if (ctrlClass == null)
			throw new ControlConstructionException(controlName, new UnsupportedControlException());

		return create(ctrlClass, instanceName, config, auxConfig);
	}
}
