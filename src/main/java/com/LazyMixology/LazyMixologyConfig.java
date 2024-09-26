package com.LazyMixology;

import net.runelite.client.config.*;

@ConfigGroup("lazyMixology")
public interface LazyMixologyConfig extends Config
{

	@ConfigSection(
			name = "General",
			description = "The highlighted and hidden item lists",
			position = 0
	)
	String generalList = "generalList";

	enum OptionPriority
	{
		Blue,
		Green,
		Red
	}
	@ConfigItem(
			position = 1,
			keyName = "enumConfigSortPriority",
			name = "Priority",
			description = "Prioritizing selection by listing them from the top",
			section = generalList
	)
	default OptionPriority enumConfigSortPriority() { return OptionPriority.Red; }
}
