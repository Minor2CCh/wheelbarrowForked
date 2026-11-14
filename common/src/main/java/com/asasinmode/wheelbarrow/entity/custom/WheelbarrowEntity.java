package com.asasinmode.wheelbarrow.entity.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asasinmode.wheelbarrow.platform.ModPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.item.ModItems;
import com.asasinmode.wheelbarrow.networking.InformYeetKeybindS2CPacket;


public class WheelbarrowEntity extends VehicleEntity {
	private static final EntityDataAccessor<Integer> OXIDATION_LEVEL = SynchedEntityData.defineId(WheelbarrowEntity.class,
            EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> IS_WAXED = SynchedEntityData.defineId(WheelbarrowEntity.class,
            EntityDataSerializers.BOOLEAN);
	public final WalkAnimationState limbAnimator = new WalkAnimationState();
	private int lerpTicks;
	private float velocityDecay;
	private double wheelbarrowYaw;
	private double wheelbarrowPitch;
	private double x;
	private double y;
	private double z;
	private float nearbySlipperiness;
	private Location location;
	private LivingEntity prevControllingPassenger;
	private float prevYawVelocity;
	private float yawVelocity;
	private boolean pressingLeft;
	private boolean pressingRight;
	private boolean pressingForward;
	private boolean pressingBack;
	private boolean sprintingPressed;
	private Entity passengerBeingYeeted = null;
	// for yeeting velocity
	private Vec3 prevServerPos = Vec3.ZERO;
	// after yeeting player entities they need to be immune to pushing for a moment
	// because otherwise they might collide with wheelbarrow and instantly lose
	// velocity
	private final Map<UUID, Integer> yeetedPlayersToPushImmuneTimestamps = new HashMap<>();

	public WheelbarrowEntity(EntityType<? extends WheelbarrowEntity> entityType, Level level) {
		super(entityType, level);
	}

	protected WheelbarrowEntity(Level level, double x, double y, double z) {
		this(ModEntities.WHEELBARROW.get(), level);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(OXIDATION_LEVEL, Type.COPPER.ordinal());
		builder.define(IS_WAXED, false);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		nbt.putString("OxidationLevel", this.getOxidationLevel().asString());
		nbt.putBoolean("IsWaxed", this.getIsWaxed());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		if (nbt.contains("OxidationLevel", Tag.TAG_STRING)) {
			this.setOxidationLevel(Type.getType(nbt.getString("OxidationLevel")));
		}
		if (nbt.contains("IsWaxed")) {
			this.setIsWaxed(nbt.getBoolean("IsWaxed"));
		}
	}

	public void setOxidationLevel(Type type) {
		this.entityData.set(OXIDATION_LEVEL, type.ordinal());
	}

	public void setOxidationLevel(int type) {
		this.entityData.set(OXIDATION_LEVEL, type);
	}

	public Type getOxidationLevel() {
		return Type.getType(this.entityData.get(OXIDATION_LEVEL));
	}

	public void setIsWaxed(boolean value) {
		this.entityData.set(IS_WAXED, value);
	}

	public boolean getIsWaxed() {
		return this.entityData.get(IS_WAXED);
	}

    @SuppressWarnings("unused")
    public static WheelbarrowEntity create(Type type, ServerLevel world, double x, double y, double z, ItemStack stack, Player player) {
        return create(type, false, world, x, y, z, stack, player);
    }
	public static WheelbarrowEntity create(Type type, boolean wax, ServerLevel world, double x, double y, double z, ItemStack stack,
                                           Player player) {

		WheelbarrowEntity wheelbarrowEntity = new WheelbarrowEntity(world, x, y, z);
		wheelbarrowEntity.setOxidationLevel(type);
        wheelbarrowEntity.setIsWaxed(wax);

		EntityType.createDefaultStackConfig(world, stack, player).accept(wheelbarrowEntity);

		return wheelbarrowEntity;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
			return false;
		}

		return super.hurt(source, amount);
	}


    @Override
	public Item getDropItem() {
        return switch (this.getOxidationLevel()) {
            case COPPER -> getIsWaxed() ? ModItems.WAXED_COPPER_WHEELBARROW.get() : ModItems.COPPER_WHEELBARROW.get();
            case EXPOSED ->
                    getIsWaxed() ? ModItems.WAXED_EXPOSED_COPPER_WHEELBARROW.get() : ModItems.EXPOSED_COPPER_WHEELBARROW.get();
            case WEATHERED ->
                    getIsWaxed() ? ModItems.WAXED_WEATHERED_COPPER_WHEELBARROW.get() : ModItems.WEATHERED_COPPER_WHEELBARROW.get();
            case OXIDIZED ->
                    getIsWaxed() ? ModItems.WAXED_OXIDIZED_COPPER_WHEELBARROW.get() : ModItems.OXIDIZED_COPPER_WHEELBARROW.get();
        };
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		} else if (!this.level().isClientSide) {
			ItemStack itemStack = player.getItemInHand(hand);
			Type oxidationLevel = this.getOxidationLevel();
			boolean isWaxed = this.getIsWaxed();

			if (itemStack.is(ItemTags.AXES)) {
				if (isWaxed) {
					setIsWaxed(false);
					this.level().playSound(null, this.blockPosition(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f,
							1.0f);
					this.spawnParticles(ParticleTypes.WAX_OFF, (ServerLevel) this.level());

					return InteractionResult.SUCCESS;
				}
				if (oxidationLevel == Type.COPPER) {
					return InteractionResult.CONSUME;
				}

				this.setOxidationLevel(oxidationLevel.ordinal() - 1);
				itemStack.hurtAndBreak(1, player, hand.equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
				this.level().playSound(null, this.blockPosition(),
						SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f,
						1.0f);
				this.spawnParticles(ParticleTypes.SCRAPE, (ServerLevel) this.level());

				return InteractionResult.CONSUME;
			} else if (itemStack.is(Items.HONEYCOMB)) {
				if (isWaxed) {
					return InteractionResult.CONSUME;
				}
				this.setIsWaxed(true);
                itemStack.consume(1, player);
				this.level().playSound(null, this.blockPosition(),
						SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS,
						1.0f,
						1.0f);
				this.spawnParticles(ParticleTypes.WAX_ON, (ServerLevel) this.level());
				return InteractionResult.SUCCESS;
			}

			if (!player.startRiding(this)) {
				return InteractionResult.PASS;
			}

			if (this.getPassengers().size() > 1 && this.getControllingPassenger() == player) {
                ModPlatform.INSTANCE.sendPacket((ServerPlayer) player, InformYeetKeybindS2CPacket.INSTANCE);
			}

			return InteractionResult.CONSUME;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void thunderHit(ServerLevel world, LightningBolt lightning) {
		Type oxidationLevel = this.getOxidationLevel();
		if (!this.getIsWaxed() && oxidationLevel != Type.COPPER) {
			this.setOxidationLevel(oxidationLevel.ordinal() - 1);
		}
	}

	@Override
	public void animateHurt(float yaw) {
		this.setHurtDir(-this.getHurtDir());
		this.setHurtTime(10);
		this.setDamage(this.getDamage() * 11.0f);
	}

	@Override
	public boolean canCollideWith(Entity other) {
		return canCollide(this, other);
	}

	public static boolean canCollide(Entity entity, Entity other) {
		return (other.canBeCollidedWith() || other.isPushable()) && !entity.isPassengerOfSameVehicle(other);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public void push(Entity entity) {
		if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
			super.push(entity);
		}
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public Direction getMotionDirection() {
		return this.getDirection();
	}

	@Override
	protected Component getTypeName() {
		return Component.translatable(this.getDropItem().getDescriptionId());
	}

	@Override
	public ItemStack getWeaponItem() {
		return new ItemStack(this.getDropItem());
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().size() < this.getMaxPassengers();
	}

	protected int getMaxPassengers() {
		return Wheelbarrow.CONFIG.getMaxPassengers() + 1;
	}

	@Override
	@Nullable
	public LivingEntity getControllingPassenger() {
		Entity entity = this.getFirstPassenger();
		return entity instanceof LivingEntity ? (LivingEntity) entity : super.getControllingPassenger();
	}

	public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack,
			boolean sprinting, boolean jumping) {
		this.pressingLeft = pressingLeft;
		this.pressingRight = pressingRight;
		this.pressingForward = pressingForward;
		this.pressingBack = pressingBack;
		this.sprintingPressed = sprinting;
		if (!pressingForward) {
			this.setSprinting(false);
		} else if (sprinting) {
			this.setSprinting(true);
		}
	}

	private void steer() {
		if (!(this.getControllingPassenger() instanceof Player playerEntity)) {
			return;
		}

		if (this.pressingLeft) {
			this.yawVelocity -= 1.5f;
			if (this.yawVelocity <= -5.0f) {
				this.yawVelocity = -5.0f;
			}
		}
		if (this.pressingRight) {
			this.yawVelocity += 1.5f;
			if (this.yawVelocity >= 5.0f) {
				this.yawVelocity = 5.0f;
			}
		}

		this.setYRot(this.getYRot() + this.yawVelocity);

		float velocity = 0.0f;
		if (this.pressingForward) {
			velocity = 0.07f;
		}
		if (this.pressingBack) {
			velocity = -0.07f;
		}

		// get player base walk speed diff, for example from
		// sprinting/swiftness/slowness
		float playerVelocityMultiplier = ((float) playerEntity.getAttributeValue(Attributes.MOVEMENT_SPEED)
				- playerEntity.getAbilities().getWalkingSpeed()) * 3 + 1.0f;

		if (playerVelocityMultiplier > 1.0f) {
			// when sprinting this is around 0.09f velocity
			playerVelocityMultiplier += 0.195f;
		}

		velocity *= playerVelocityMultiplier;

		this.moveRelative(1.0f, new Vec3(0.0f, 0.0f, velocity));
	}

	@Override
	public boolean isSprinting() {
		if (this.getControllingPassenger() instanceof Player playerEntity) {
			return playerEntity.isSprinting();
		}
		return super.isSprinting();
	}

	@Override
	public void setSprinting(boolean sprinting) {
		for (Entity entity : this.getPassengers()) {
			if (entity instanceof Player playerEntity) {
				playerEntity.setSprinting(sprinting);
			}
		}
		super.setSprinting(sprinting);
	}

	protected void updateLimbs(float posDelta) {
		// whatever makes wheel turning look good
		float speed = Math.min(posDelta * 2.0f, 1.0f);
		// set speed first so that there is no spin after stop
		this.limbAnimator.setSpeed(speed);
		this.limbAnimator.update(speed, 1.0f);
	}

	// up to horse/iron golem/spider
	public boolean canBeYoinked(Entity entity) {
		return entity.getBbWidth() <= 1.4;
	}

	@Override
	public void tick() {
		boolean isServer = !this.level().isClientSide;

		// every half a second cleanup push immune entities timestamps
		if (this.tickCount % 10 == 0) {
			this.yeetedPlayersToPushImmuneTimestamps.entrySet().removeIf((entry) -> {
				if (entry.getValue() <= this.tickCount) {
					return true;
				}
				return false;
			});
		}

		this.location = this.checkLocation();

		if (this.getHurtTime() > 0) {
			this.setHurtTime(this.getHurtTime() - 1);
		}

		if (this.getDamage() > 0.0f) {
			this.setDamage(this.getDamage() - 1.0f);
		}

		super.tick();

		this.updatePositionAndRotation();

		if (this.isControlledByLocalInstance()) {
			this.updateVelocity();
			this.steer();
			this.move(MoverType.SELF, this.getDeltaMovement());
		} else {
			this.setDeltaMovement(Vec3.ZERO);
			this.prevServerPos = this.position();
		}

		double yawRad = Math.toRadians(this.getYRot());
		double yawSin = Math.sin(yawRad);
		double yawCos = Math.cos(yawRad);
		double deltaX = this.getX() - this.xOld;
		double deltaZ = this.getZ() - this.zOld;
		double movementX = deltaX * yawCos + deltaZ * yawSin;
		double movementZ = -deltaX * yawSin + deltaZ * yawCos;
		int direction = Math.atan2(movementZ, movementX) >= 0.0f ? 1 : -1;
		float posDelta = (float) Mth.length(deltaX, deltaZ);

		this.updateLimbs(posDelta * direction);

		this.checkInsideBlocks();

		LivingEntity controllingPassenger = this.getControllingPassenger();
		this.prevControllingPassenger = controllingPassenger;

		boolean isControlledByPlayer = controllingPassenger instanceof Player;

		List<Entity> list = this.level().getEntities(this,
				this.getBoundingBox().expandTowards(0.2, 0.1, 0.2),
				(entity) -> !entity.hasPassenger(this) // wheelbarrow not passenger of
						&& !this.hasPassenger(entity) // entity not passenger of wheelbarrow
						&& EntitySelector.pushableBy(this).test(entity));

		for (Entity entity : list) {
			boolean canYoink = isServer
					&& isControlledByPlayer
					&& this.getPassengers().size() < this.getMaxPassengers()
					&& !entity.isPassenger()
					&& entity instanceof LivingEntity
					&& !(entity instanceof Player)
					&& this.canBeYoinked(entity);

			if (canYoink) {
				if (entity.startRiding(this)) {
                    ModPlatform.INSTANCE.sendPacket((ServerPlayer) controllingPassenger, InformYeetKeybindS2CPacket.INSTANCE);
				}

			} else {
				if (!(entity instanceof Player)
						|| this.yeetedPlayersToPushImmuneTimestamps.getOrDefault(entity.getUUID(), Integer.MIN_VALUE) < this.tickCount) {
					this.push(entity);
				}
			}
		}

		Type oxidationLevel = this.getOxidationLevel();
		if (isServer && oxidationLevel != Type.OXIDIZED && !this.getIsWaxed()) {
			int randomTickSpeed = this.level().getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
			RandomSource random = this.level().getRandom();
			// once a day, same as copper blocks + include random tick speed modifier
			// (1 / 24000 / defaultRandomTickSpeed) * randomTickSpeed
			if (random.nextFloat() < 0.0000139f * randomTickSpeed) {
				this.setOxidationLevel(oxidationLevel.ordinal() + 1);
			}
		}
	}

	private void updatePositionAndRotation() {
		if (this.isControlledByLocalInstance()) {
			this.lerpTicks = 0;
			this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpTicks > 0) {
			this.lerpPositionAndRotationStep(this.lerpTicks, this.x, this.y, this.z, this.wheelbarrowYaw, this.wheelbarrowPitch);
			--this.lerpTicks;
		}
	}

    @Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch,
			int interpolationSteps) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.wheelbarrowYaw = yaw;
		this.wheelbarrowPitch = pitch;
		this.lerpTicks = 10;
	}

	@Override
	public double lerpTargetX() {
		return this.lerpTicks > 0 ? this.x : this.getX();
	}

	@Override
	public double lerpTargetY() {
		return this.lerpTicks > 0 ? this.y : this.getY();
	}

	@Override
	public double lerpTargetZ() {
		return this.lerpTicks > 0 ? this.z : this.getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return this.lerpTicks > 0 ? (float) this.wheelbarrowPitch : this.getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return this.lerpTicks > 0 ? (float) this.wheelbarrowYaw : this.getYRot();
	}

	public float getPrevYawVelocity() {
		return this.prevYawVelocity;
	}

	public float getYawVelocity() {
		return this.yawVelocity;
	}

	private void updateVelocity() {
		double yMulitplier = 1.0;
		double yMod = this.isNoGravity() ? 0.0 : -0.04;
		this.velocityDecay = 0.05f;

		if (this.location == Location.UNDER_FLOWING_WATER) {
			yMod = -0.03;
			this.velocityDecay = 0.3f;
		} else if (this.location == Location.UNDER_WATER) {
			this.velocityDecay = 0.3f;
			yMulitplier = 0.8;
		} else if (this.location == Location.IN_AIR) {
			this.velocityDecay = 0.5f;
		} else if (this.location == Location.ON_LAND) {
			this.velocityDecay = this.nearbySlipperiness;
			if (this.getControllingPassenger() instanceof Player) {
				this.nearbySlipperiness /= 2.0f;
			}
		}

		Vec3 velocity = this.getDeltaMovement();
		double y = (velocity.y + yMod) * yMulitplier;

		// terminal velocity so it doesn't accelerate infinitely
		if (y < -1.0) {
			y = -1.0;
		}

		this.prevYawVelocity = this.yawVelocity;
        this.yawVelocity = (float) (this.yawVelocity * 0.6);
		this.setDeltaMovement(velocity.x * (double) this.velocityDecay, y, velocity.z * (double) this.velocityDecay);

		double velocityLength = this.getDeltaMovement().horizontalDistance();

		// bumped into something or stopped not sure if thats how you do it
		if (velocityLength <= 0.07) {
			if (this.isSprinting() && !this.sprintingPressed) {
				this.setSprinting(false);
			}
		}
	}

	private Location checkLocation() {
		Location location = this.getUnderWaterLocation();
		if (location != null) {
			return location;
		}

		float slipperiness = this.getNearbySlipperiness();
		if (slipperiness > 0.0f) {
			this.nearbySlipperiness = slipperiness;
			return Location.ON_LAND;
		} else {
			return Location.IN_AIR;
		}
	}

	public float getNearbySlipperiness() {
		AABB box = this.getBoundingBox();
        AABB box2 = new AABB(box.minX, box.minY - 0.001, box.minZ, box.maxX, box.minY, box.maxZ);
		int i = Mth.floor(box2.minX) - 1;
		int j = Mth.ceil(box2.maxX) + 1;
		int k = Mth.floor(box2.minY) - 1;
		int l = Mth.ceil(box2.maxY) + 1;
		int m = Mth.floor(box2.minZ) - 1;
		int n = Mth.ceil(box2.maxZ) + 1;
		VoxelShape voxelShape = Shapes.create(box2);
		float f = 0.0f;
		int o = 0;
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int p = i; p < j; ++p) {
			for (int q = m; q < n; ++q) {
				int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
				if (r != 2) {
					for (int s = k; s < l; ++s) {
						if (r <= 0 || s != k && s != l - 1) {
							mutable.set(p, s, q);
							BlockState blockState = this.level().getBlockState(mutable);
							if (!(blockState.getBlock() instanceof WaterlilyBlock) && Shapes.joinIsNotEmpty(
									blockState.getCollisionShape(this.level(), mutable).move((double) p, (double) s, (double) q),
									voxelShape, BooleanOp.AND)) {
								f += blockState.getBlock().getFriction();
								++o;
							}
						}
					}
				}
			}
		}

		return f / (float) o;
	}

	@Nullable
	private Location getUnderWaterLocation() {
		AABB box = this.getBoundingBox();
		double d = box.maxY + 0.001;
		int i = Mth.floor(box.minX);
		int j = Mth.ceil(box.maxX);
		int k = Mth.floor(box.maxY);
		int l = Mth.ceil(d);
		int m = Mth.floor(box.minZ);
		int n = Mth.ceil(box.maxZ);
		boolean isUnderWater = false;
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int o = i; o < j; ++o) {
			for (int p = k; p < l; ++p) {
				for (int q = m; q < n; ++q) {
					mutable.set(o, p, q);
					FluidState fluidState = this.level().getFluidState(mutable);
					if (fluidState.is(FluidTags.WATER)
							&& d < (double) ((float) mutable.getY() + fluidState.getHeight(this.level(), mutable))) {
						if (!fluidState.isSource()) {
							return Location.UNDER_FLOWING_WATER;
						}

						isUnderWater = true;
					}
				}
			}
		}

		return isUnderWater ? Location.UNDER_WATER : null;
	}

	@Override
	protected void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
		if (this.isPassenger()) {
			return;
		}

		if (onGround) {
			this.resetFallDistance();
		} else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER)
				&& heightDifference < 0.0) {
			this.fallDistance -= (float) heightDifference;
		}
	}

	@Override
	public float maxUpStep() {
		if (this.getControllingPassenger() instanceof Player) {
			return 1.0f;
		}

		return 0.5f;
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
		float zOffset = -0.8f;
		float yOffset = 0.6f;
		boolean isControllingPassenger = passenger == this.getControllingPassenger();
		boolean isPlayer = passenger instanceof Player;

		if ((isControllingPassenger && isPlayer) || (!isPlayer && this.getPassengers().indexOf(passenger) > 1)) {
			passenger.setPose(Pose.STANDING);
		} else {
			passenger.setPose(Pose.SITTING);
		}

		if (!isControllingPassenger || !isPlayer) {
			zOffset = 0.2f;
			yOffset = 0.4f;
			zOffset += Math.max((passenger.getBbWidth() - 1.0f) / 2.0f, 0.0f);
		}

		List<Entity> passengers = this.getPassengers();
		int index = passengers.indexOf(passenger);
		if (index != 0) {
			int offset = this.getControllingPassenger() instanceof Player ? 1 : 0;
			for (Entity entity : passengers.subList(offset, index)) {
				double ridingOffset = entity.getAttachments().get(EntityAttachment.VEHICLE, 0, passenger.getYRot())
						.y();
				yOffset += entity.getBbHeight() - (float) ridingOffset;
			}
		}

		return new Vec3(0.0f, yOffset, zOffset).yRot((float) -Math.toRadians(this.getYRot()));
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction positionUpdater) {
		super.positionRider(passenger, positionUpdater);

		boolean isControllingPassenger = passenger == this.getControllingPassenger();
		boolean isPlayer = passenger instanceof Player;

		if (isControllingPassenger && isPlayer) {
			passenger.setPose(Pose.STANDING);
		} else {
			passenger.setPose(Pose.SITTING);
		}

		if (passenger.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
			return;
		}

		passenger.setYRot(passenger.getYRot() + this.yawVelocity);
		passenger.setYHeadRot(passenger.getYHeadRot() + this.yawVelocity);

		this.clampPassengerYaw(passenger);
	}

	protected void clampPassengerYaw(Entity passenger) {
		if (passenger != this.getControllingPassenger() || !(passenger instanceof Player)) {
			return;
		}
		passenger.setYBodyRot(this.getYRot());
		float f = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
		float g = Mth.clamp(f, -105.0f, 105.0f);
		passenger.yRotO += g - f;
		passenger.setYRot(passenger.getYRot() + g - f);
		passenger.setYHeadRot(passenger.getYRot());
	}

	@Override
	public void onPassengerTurned(Entity passenger) {
		this.clampPassengerYaw(passenger);
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		if (this.passengerBeingYeeted == passenger) {
			this.passengerBeingYeeted = null;

			float offset = 0.15f + passenger.getBbWidth() * 0.1f;
			double yawRad = Math.toRadians(this.getYRot());
			double yawSin = -Math.sin(yawRad);
			double yawCos = Math.cos(yawRad);
			double largerSinCos = Math.max(Math.abs(yawSin), Math.abs(yawCos));
			double x = yawSin * offset / largerSinCos;
			double y = 0.3;
			double z = yawCos * offset / largerSinCos;
			Vec3 currentVelocity = this.position().subtract(this.prevServerPos);
			double length = currentVelocity.length();

			if (length >= 0.3) {
				y += length * 0.2;
			}

			passenger.setDeltaMovement(currentVelocity.scale(1.5).add(x, y, z));
			passenger.hurtMarked = true;

			if (passenger instanceof Player) {
				// disable pushing for 2 seconds for yeeted players
				this.yeetedPlayersToPushImmuneTimestamps.put(passenger.getUUID(), this.tickCount +
						40);
			}

			return passenger.position().add(currentVelocity);
		}

		double x, z, dismountYOffset;
		BlockPos blockPos;

		if (passenger == this.prevControllingPassenger) {
			float offset = -0.6f;
			double yawRad = Math.toRadians(this.getYRot());
			double yawSin = -Math.sin(yawRad);
			double yawCos = Math.cos(yawRad);
			double largerSinCos = Math.max(Math.abs(yawSin), Math.abs(yawCos));
			x = passenger.getX() + yawSin * offset / largerSinCos;
			z = passenger.getZ() + yawCos * offset / largerSinCos;

			blockPos = BlockPos.containing(x, this.getY(), z);
			dismountYOffset = Math.max(0, this.level().getBlockFloorHeight(blockPos));
		} else {
			Vec3 offsetVec = WheelbarrowEntity.getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO,
					passenger.getBbWidth(), passenger.getYRot());

			x = this.getX() + offsetVec.x;
			z = this.getZ() + offsetVec.z;
			blockPos = BlockPos.containing(x, this.getBoundingBox().maxY, z);
			dismountYOffset = this.level().getBlockFloorHeight(blockPos);
		}

		return new Vec3(x, blockPos.getY() + dismountYOffset, z);
	}

	// overriden because the default implementation makes it hover few blocks above
	// water surface
	@Override
	public void onAboveBubbleCol(boolean drag) {
	}
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }

	private void spawnParticles(SimpleParticleType particleType, ServerLevel world) {
		double yawRad = Math.toRadians(this.getYRot());
		double yawSin = Math.sin(yawRad);
		double yawCos = Math.cos(yawRad);

		// inside
		for (int i = 0; i < 6; i++) {
			double particleX = random.nextDouble() * 0.4 * (double) (random.nextBoolean()
					? 1
					: -1);
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + random.nextDouble() * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// left
		for (int i = 0; i < 8; i++) {
			double particleX = 0.5;
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + random.nextDouble() * 0.625 * (double) (random.nextBoolean() ? 1 : -1);
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// right
		for (int i = 0; i < 8; i++) {
			double particleX = -0.5;
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + random.nextDouble() * 0.625 * (double) (random.nextBoolean() ? 1 : -1);
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// back
		for (int i = 0; i < 4; i++) {
			double particleX = random.nextDouble() * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = -0.55;
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// front
		for (int i = 0; i < 4; i++) {
			double particleX = random.nextDouble() * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.8;
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}
	}

	private void spawnParticle(ServerLevel world, SimpleParticleType particleType, double x, double y, double z,
                               double yawSin, double yawCos) {
		world.sendParticles(particleType,
				this.getX() + x * yawCos - z * yawSin,
				this.getY() + 0.55 + y,
				this.getZ() + z * yawCos + x * yawSin,
				1,
				random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
				random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
				random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
				random.nextDouble() * 0.5);
	}

	public void yeetLastPassenger() {
		List<Entity> passengers = this.getPassengers();
		if (passengers.size() <= 1) {
			return;
		}

		Entity passenger = passengers.getLast();
		this.passengerBeingYeeted = passenger;
		passenger.stopRiding();
	}

	public enum Type {
		COPPER("copper"),
		EXPOSED("exposed"),
		WEATHERED("weathered"),
		OXIDIZED("oxidized");

		private final String type;

		Type(String type) {
			this.type = type;
		}

		public String asString() {
			return type;
		}

		public static Type getType(String value) {
			for (Type enumConstant : Type.values()) {
				if (enumConstant.asString().equals(value)) {
					return enumConstant;
				}
			}
			throw new IllegalArgumentException("No enum constant with value: " + value);
		}

		public static Type getType(int ordinal) {
			if (ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			throw new IllegalArgumentException("No enum constant with ordinal value: " + ordinal);
		}
	}

	enum Location {
		ON_LAND,
		IN_AIR,
		UNDER_WATER,
		UNDER_FLOWING_WATER;
	}
}
