package com.LazyMixology;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Lazy Mixology",
	description = "Highlights what to click"
)
public class LazyMixologyPlugin extends Plugin
{
	//ThaEstonian didn't do anything
	@Inject
	private Client client;

	@Inject
	private LazyMixologyConfig config;

	@Inject
	public OverlayManager overlayManager;


	@Inject
	public LazyMixologyOverlay overlay;
	public List<PotionValues> potionList = new ArrayList<>();
	public List<Object> getSortedPotionValues() {
		// Get the widget text values
		Widget parentWidget = client.getWidget(882, 2);
		int[] textIndices = {2, 4, 6};
		int[] spriteIndices = {1, 3, 5};

		Widget[] dynamicChildren = parentWidget.getDynamicChildren();

		List<PotionValues> selectedPotions = new ArrayList<>();
		List<Integer> spriteIds = new ArrayList<>();

		int playerLevel = client.getBoostedSkillLevel(net.runelite.api.Skill.HERBLORE);

		// Collect the potions matching the widget text and their sprite IDs
		for (int i = 0; i < textIndices.length; i++) {
			int textIndex = textIndices[i];
			int spriteIndex = spriteIndices[i];

			if (textIndex < dynamicChildren.length && spriteIndex < dynamicChildren.length) {
				Widget textWidget = dynamicChildren[textIndex];
				Widget spriteWidget = dynamicChildren[spriteIndex];

				String widgetText = textWidget.getText();
				int spriteId = spriteWidget.getSpriteId();

				// Find the corresponding potion by matching the text
				for (PotionValues potion : potionList) {
					if (potion.getName().equals(widgetText) && potion.getLevel() <= playerLevel) {
						selectedPotions.add(potion);
						spriteIds.add(spriteId); // Add corresponding sprite ID
						break;
					}
				}
			}
		}

		// If no potions are available at the player's level, find the highest level potion they can make
		if (selectedPotions.isEmpty()) {
			PotionValues highestAvailablePotion = null;
			int highestAvailableSpriteId = -1;

			for (PotionValues potion : potionList) {
				if (potion.getLevel() <= playerLevel && (highestAvailablePotion == null || potion.getLevel() > highestAvailablePotion.getLevel())) {
					highestAvailablePotion = potion;
				}
			}

			if (highestAvailablePotion != null) {
				// Find a valid sprite ID from the visible widgets
				for (int i = 0; i < textIndices.length; i++) {
					Widget textWidget = dynamicChildren[textIndices[i]];
					Widget spriteWidget = dynamicChildren[spriteIndices[i]];

					if (textWidget != null && spriteWidget != null) {
						highestAvailableSpriteId = spriteWidget.getSpriteId();
						break;  // Use the first valid sprite ID we find
					}
				}

				// If we couldn't find a valid sprite ID, use a default one
				if (highestAvailableSpriteId == -1) {
					highestAvailableSpriteId = 5673;  // Default to alembic sprite ID
				}

				return Arrays.asList(highestAvailablePotion.getBlue(), highestAvailablePotion.getGreen(), highestAvailablePotion.getRed(), highestAvailableSpriteId);
			}

			// If no potions are available at all, return an empty list
			return new ArrayList<>();
		}

		// Determine the potion with the highest priority
		LazyMixologyConfig.OptionPriority priority = config.enumConfigSortPriority();
		int highestPriorityIndex = -1;
		int highestValue = Integer.MIN_VALUE;

		// Iterate over the selected potions and find the highest based on the selected priority
		for (int i = 0; i < selectedPotions.size(); i++) {
			PotionValues potion = selectedPotions.get(i);
			int value = getPriorityValue(potion, priority);

			if (value > highestValue) {
				highestValue = value;
				highestPriorityIndex = i; // Track the index of the highest-priority potion
			}
		}

		// Return the values of the potion with the highest priority, along with its sprite ID
		if (highestPriorityIndex != -1) {
			PotionValues highestPriorityPotion = selectedPotions.get(highestPriorityIndex);
			int correspondingSpriteId = spriteIds.get(highestPriorityIndex);

			// Return the blue, green, red values and the sprite ID of the highest-priority potion
			return Arrays.asList(highestPriorityPotion.getBlue(), highestPriorityPotion.getGreen(), highestPriorityPotion.getRed(), correspondingSpriteId);
		}

		// Return an empty list if no potions were found
		return new ArrayList<>();
	}


	// Helper method to get the value corresponding to the current sorting priority
	public int getPriorityValue(PotionValues potion, LazyMixologyConfig.OptionPriority priority) {
		switch (priority) {
			case Blue:
				return potion.blue;
			case Green:
				return potion.green;
			default:
			case Red:
				return potion.red;
		}
	}


	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		initiateValues();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	public void initiateValues()
	{
		potionList.add(new PotionValues("Mammoth-might mix", 30, 0, 0, 60));
		potionList.add(new PotionValues("Mystic mana amalgam", 20, 10, 0, 60));
		potionList.add(new PotionValues("Marley's moonlight", 20, 0, 10, 60));
		potionList.add(new PotionValues("Azure aura mix", 10, 20, 0, 68));
		potionList.add(new PotionValues("Alco-augmentator", 0, 30, 0, 76));
		potionList.add(new PotionValues("Mixalot", 10, 10, 10, 64));
		potionList.add(new PotionValues("Aqualux amalgam", 0, 20, 10, 72));
		potionList.add(new PotionValues("Megalite liquid", 10, 0, 20, 80));
		potionList.add(new PotionValues("Anti-leech lotion", 0, 10, 20, 84));
		potionList.add(new PotionValues("Liplack liquor", 0, 0, 30, 86));
	}
	public boolean herbSpawned = false;
	public String message;
	public String location;
	@Subscribe
	public void onChatMessage(ChatMessage event) {
		message = event.getMessage();
		if (event.getType() == ChatMessageType.GAMEMESSAGE) {
			message = event.getMessage();
		}
		if (event.getType() == ChatMessageType.SPAM) {
			message = event.getMessage();
		}

		// Check if the message contains any of the specified phrases
		if(Objects.equals(message, "<col=229628>A herb to the north east has matured...")) {
		//if (Objects.equals(message, "You finish concentrating the <col=a53fff>Aqualux amalgam</col>.")) {
			herbSpawned = true;
			System.out.println(herbSpawned + "NE");
			location = "NE";
		} else if (Objects.equals(message,"<col=229628>A herb to the north west has matured...")) {
			herbSpawned = true;
			System.out.println(herbSpawned+ "NW");
			location = "NW";
		} else if (Objects.equals(message,"<col=229628>A herb to the south east has matured...")) {
			herbSpawned = true;
			System.out.println(herbSpawned+ "SE");
			location = "SE";
		} else if (Objects.equals(message,"<col=229628>A herb to the south west has matured...")) {
			herbSpawned = true;
			System.out.println(herbSpawned+ "SW");
			location = "SW";
		}
		else if (Objects.equals(message,"<col=ff3045>The matured herb has died.")) {
			herbSpawned = false;
			System.out.println(herbSpawned+ "died");
		}
		else if (Objects.equals(message,"You collect a handful of digweed.")) {
			herbSpawned = false;
			System.out.println(herbSpawned+ "collected");
		}
	}

	@Provides
	LazyMixologyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LazyMixologyConfig.class);
	}
}
