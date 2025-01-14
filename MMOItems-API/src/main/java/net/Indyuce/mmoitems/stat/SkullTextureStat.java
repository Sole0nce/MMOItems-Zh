package net.Indyuce.mmoitems.stat;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.SkullTextureData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SkullTextureStat extends ItemStat<SkullTextureData, SkullTextureData> {
	public SkullTextureStat() {
		super("SKULL_TEXTURE", VersionMaterial.PLAYER_HEAD.toMaterial(), "头颅纹理",
				new String[] { "头部纹理 &n参数&7 ", "可以在头颅数据库中找到" }, new String[] { "all" },
				VersionMaterial.PLAYER_HEAD.toMaterial());
	}

	@Override
	public SkullTextureData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
		ConfigurationSection config = (ConfigurationSection) object;

		String value = config.getString("value");
		Validate.notNull(value, "无法加载头骨纹理值");

		String format = config.getString("uuid");
		Validate.notNull(format, "找不到头骨纹理 UUID: 重新输入您的头骨纹理值, 系统将随机选择一个");

		SkullTextureData skullTexture = new SkullTextureData(new GameProfile(UUID.fromString(format), "SkullTexture"));
		skullTexture.getGameProfile().getProperties().put("textures", new Property("textures", value));
		return skullTexture;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<SkullTextureData> statData) {
		lore.add(ChatColor.GRAY + "当前值: " + (statData.isPresent() ? ChatColor.GREEN + "提供纹理值 " : ChatColor.RED + "None"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + "► 左键单击可更改此值");
		lore.add(ChatColor.YELLOW + AltChar.listDash + "► 右键单击可删除该值");
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		inv.getEditedSection().set("skull-texture.value", message);
		inv.getEditedSection().set("skull-texture.uuid", UUID.randomUUID().toString());
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + "成功更改为 " + message + ".");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull SkullTextureData data) {
		if (item.getItemStack().getType() != VersionMaterial.PLAYER_HEAD.toMaterial())
			return;

		try {
			Field profileField = item.getMeta().getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(item.getMeta(), ((SkullTextureData) data).getGameProfile());
		} catch (NoSuchFieldException | IllegalAccessException exception) {
			throw new IllegalArgumentException(exception.getMessage());
		}
	}
	/**
	 * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
	 * Alas this is an empty array
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull SkullTextureData data) { return new ArrayList<>(); }

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set(getPath(), null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + getName() + ".");
		} else
			new StatEdition(inv, this).enable("在聊天中输入您想要的文字");
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		try {
			Field profileField = mmoitem.getNBT().getItem().getItemMeta().getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			mmoitem.setData(ItemStats.SKULL_TEXTURE, new SkullTextureData((GameProfile) profileField.get(mmoitem.getNBT().getItem().getItemMeta())));
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {}
	}

	/**
	 * This stat is saved not as a custom tag, but as the vanilla Head Texture itself.
	 * Alas this method returns null.
	 */
	@Nullable
	@Override
	public SkullTextureData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) { return null; }

	@NotNull
	@Override
	public SkullTextureData getClearStatData() { return new SkullTextureData(new GameProfile(UUID.fromString("df930b7b-a84d-4f76-90ac-33be6a5b6c88"), "gunging")); }
}
