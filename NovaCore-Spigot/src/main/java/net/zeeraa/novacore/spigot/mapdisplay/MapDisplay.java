package net.zeeraa.novacore.spigot.mapdisplay;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import net.coobird.thumbnailator.Thumbnails;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependantUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependantMetarial;
import net.zeeraa.novacore.spigot.mapdisplay.renderer.DisplayRenderer;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;
import net.zeeraa.novacore.spigot.world.WorldUtils;

public class MapDisplay {
	private World world;

	protected UUID uuid;

	protected Dimension dimensions;
	protected Dimension resolution;

	protected UUID[][] itemFrameUuids;
	protected ItemFrame[][] itemFrames;

	protected DisplayRenderer[][] renderers;

	protected boolean persistent;

	protected String name;

	protected File cacheFile;

	public void setImage(BufferedImage image) throws Exception {
		this.setImage(image, true);
	}

	public void setImage(BufferedImage image, boolean cache) throws Exception {
		if (image == null) {
			for (int i = 0; i < itemFrames.length; i++) {
				for (int j = 0; j < itemFrames[i].length; j++) {
					if (renderers[i][j] == null) {
						continue;
					}
					renderers[i][j].setImage(null);
				}
			}

			return;
		}

		Dimension imageDimension = getScaledDimension(new Dimension(image.getWidth(), image.getHeight()), getResolution());
		BufferedImage resized = resizeImage(image, imageDimension.width, imageDimension.height);

		BufferedImage realImage = new BufferedImage(resolution.width, resolution.height, BufferedImage.TYPE_INT_RGB);

		drawImageOnImage(resized, realImage, 0, 0);

		for (int y = 0; y < itemFrames.length; y++) {
			for (int x = 0; x < itemFrames[y].length; x++) {
				if (renderers[y][x] == null) {
					continue;
				}
				Image part = cropImage(realImage, new Rectangle(x * 128, y * 128, 128, 128));

				renderers[y][x].setImage(part);
			}
		}

		this.clearPlayerCache();

		if (persistent) {
			if (cache) {
				try {
					cacheFile.getParentFile().mkdirs();
					ImageIO.write(realImage, "png", cacheFile);
				} catch (Exception e) {
					Log.error("Failed to save persistent data for MapDisplay " + name + ". " + e.getClass().getName() + " " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create a MapDisplay
	 * 
	 * @param world          The {@link World} that the display is located in
	 * @param itemFrameUuids Array of item frames
	 * @throws MissingItemFrameException if there is an item frame missing
	 */
	public MapDisplay(World world, UUID[][] itemFrameUuids, boolean persistent, String name) {
		this.world = world;
		this.itemFrameUuids = itemFrameUuids;
		this.persistent = persistent;
		this.name = name;
		this.uuid = UUID.randomUUID();

		this.cacheFile = new File(world.getWorldFolder().getPath() + File.separator + "novacore" + File.separator + "mapdisplays" + File.separator + "cache" + File.separator + name + ".png");

		dimensions = new Dimension(itemFrameUuids[0].length, itemFrameUuids.length);
		resolution = new Dimension((int) dimensions.getWidth() * 128, (int) dimensions.getHeight() * 128);

		itemFrames = new ItemFrame[itemFrameUuids.length][itemFrameUuids[0].length];

		for (int i = 0; i < itemFrameUuids.length; i++) {
			for (int j = 0; j < itemFrameUuids[i].length; j++) {
				ItemFrame itemFrame = (ItemFrame) WorldUtils.getEntityByUUID(world, itemFrameUuids[i][j]);

				if (itemFrame == null) {
					throw new MissingItemFrameException("Could not find item frame with uuid " + itemFrameUuids[i][j].toString());
				}

				itemFrames[i][j] = itemFrame;
			}
		}

		renderers = new DisplayRenderer[itemFrameUuids.length][itemFrameUuids[0].length];

		Log.debug("MapDisplay", "Map diplay initiated with a resolution of " + resolution.width + "x" + resolution.height + ". grid size is " + dimensions.width + "x" + dimensions.height);
	}

	public void delete() {
		if (persistent) {
			if (MapDisplayManager.getInstance().getDataFile(world, name).exists()) {
				MapDisplayManager.getInstance().getDataFile(world, name).delete();
			}

			if (cacheFile.exists()) {
				cacheFile.delete();
			}
		}

		for (int i = 0; i < itemFrames.length; i++) {
			for (int j = 0; j < itemFrames[i].length; j++) {
				itemFrames[i][j].setItem(ItemBuilder.AIR);
			}
		}

		MapDisplayManager.getInstance().getMapDisplays().remove(this);
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public UUID[][] getItemFrameUuids() {
		return itemFrameUuids;
	}

	public ItemFrame[][] getItemFrames() {
		return itemFrames;
	}

	public World getWorld() {
		return world;
	}

	public Dimension getDimensions() {
		return dimensions;
	}

	public Dimension getResolution() {
		return resolution;
	}

	public ItemFrame getItemFrame(int x, int y) {
		return itemFrames[x][y];
	}

	public void setupMaps() {
		for (int i = 0; i < itemFrames.length; i++) {
			for (int j = 0; j < itemFrames[i].length; j++) {
				ItemStack item = itemFrames[i][j].getItem();

				MapView view = null;
				if (item != null) {
					if (item.getType() == VersionIndependantUtils.get().getItemStack(VersionIndependantMetarial.FILLED_MAP).getType()) {
						view = NovaCore.getInstance().getVersionIndependentUtils().getAttachedMapView(item);
					}
				}

				ItemFrame frame = itemFrames[i][j];

				item = VersionIndependantUtils.get().getItemStack(VersionIndependantMetarial.FILLED_MAP);

				if (view == null) {
					view = Bukkit.createMap(world);
					view.setScale(Scale.FARTHEST);
				}

				for (MapRenderer old : view.getRenderers()) {
					view.removeRenderer(old);
				}
				DisplayRenderer renderer = new DisplayRenderer();
				view.addRenderer(renderer);
				renderers[i][j] = renderer;

				NovaCore.getInstance().getVersionIndependentUtils().attachMapView(item, view);

				frame.setItem(item);
				frame.setRotation(Rotation.NONE);
			}
		}
	}

	public void clearPlayerCache() {
		for (int i = 0; i < renderers.length; i++) {
			for (int j = 0; j < renderers[i].length; j++) {
				renderers[i][j].clearPlayerCache();
			}
		}
	}

	public void tryLoadFromCache() {
		try {
			cacheFile.getParentFile().mkdirs();

			if (cacheFile.exists()) {
				Log.trace("MapDisplay", "Reading cache file " + cacheFile.getPath());
				BufferedImage image = ImageIO.read(cacheFile);

				setImage(image, false);
			}
		} catch (Exception e) {
			Log.error("Failed to load persistent data for MapDisplay " + name + ". " + e.getClass().getName() + " " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(originalImage).size(targetWidth, targetHeight).outputFormat("JPEG").outputQuality(1).toOutputStream(outputStream);
		byte[] data = outputStream.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return ImageIO.read(inputStream);
	}

	private static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
		BufferedImage dest = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
		return dest;
	}

	private static Dimension getScaledDimension(Dimension imageSize, Dimension boundary) {

		double widthRatio = boundary.getWidth() / imageSize.getWidth();
		double heightRatio = boundary.getHeight() / imageSize.getHeight();
		double ratio = Math.min(widthRatio, heightRatio);

		return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
	}

	private static void drawImageOnImage(BufferedImage smaller, BufferedImage larger, int x, int y) {
		larger.getGraphics().drawImage(smaller, x, y, null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UUID) {
			return this.getUuid() == (UUID) obj;
		}

		if (obj instanceof MapDisplay) {
			return this.getUuid() == ((MapDisplay) obj).getUuid();
		}

		return false;
	}

	public boolean isEntityPartOfDisplay(Entity entity) {
		for (int i = 0; i < itemFrames.length; i++) {
			for (int j = 0; j < itemFrames[i].length; j++) {
				if (entity.getUniqueId() == itemFrames[i][j].getUniqueId()) {
					return true;
				}
			}
		}
		return false;
	}
}