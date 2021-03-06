package ws.zettabyte.oldowanobsidian.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ws.zettabyte.oldowanobsidian.util.DamageSourcePure;

import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemMacuahuitl extends ItemSword {
	protected ToolMaterial toolMaterial;
	
	@Override
	public String getToolMaterialName() {
		return toolMaterial.toString();
	}
	protected float damage = 1.0F;
	protected final float swordDmg;
	protected float knockbackBoost = 2.0F;
	//Total damage to creeper = dmg + (creeperBonus * dmg), so keep it low.
	protected float creeperBonus = 0.534F;
	
	private static Field fuseTimeField = null;
	
	private float efficiency = 1.0F;
	
	protected ArrayList<PotionEffect> sacrificeBuffs = null; //Don't add anything? No sac behavior.
	
	private static DamageSourcePure sacrificeDamage = new DamageSourcePure();
	
	//Get the fuse time field
	private static void doCreeperReflection(EntityCreeper creep)
	{
		int searchVal = 30;
		Field[] creepFields = EntityCreeper.class.getFields();
		for(int i = 0; i < creepFields.length; ++i)
		{
			if(creepFields[i].getType().equals(int.class))
			{
				try {
					if(creepFields[i].getInt(creep) == searchVal)
					{
						fuseTimeField = creepFields[i];
						creepFields[i].setAccessible(true);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public ItemMacuahuitl(ToolMaterial mat) {
		super(mat);
        this.swordDmg = 4.0F + mat.getDamageVsEntity();
        this.damage = 4.0F + mat.getDamageVsEntity();
        //this.material = mat;
        //this.maxStackSize = 1;
		//Fragile wood-based weapon body.
        this.setMaxDamage(mat.getMaxUses()/8);
        //this.setCreativeTab(CreativeTabs.tabCombat);
        
        efficiency = mat.getEfficiencyOnProperMaterial();
        
        this.toolMaterial = mat;
	}
	//In many cases you'll want the damage to be different from that of the tool material.
	public ItemMacuahuitl(ToolMaterial mat, float dmg) {
		this(mat);
		damage = dmg;
	}
	protected ArrayList<String> extraInfo = new ArrayList<String>(2);
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
        final int lineMax = 36;
		for(int i = 0; i < extraInfo.size(); ++i)
		{
			for(int stringpos = 0; stringpos < extraInfo.get(i).length(); stringpos += lineMax) {
                int end = stringpos + lineMax;
                if(end > extraInfo.get(i).length())
                {
                    end = extraInfo.get(i).length();
                }
                list.add(extraInfo.get(i).substring(stringpos, end));
            }
			//list.add(extraInfo.get(i));
		}
	}
	public ItemMacuahuitl addTooltip(String str)
	{
		extraInfo.add(str);
		return this;
	}
	
	public ItemMacuahuitl addTooltipLocalize(String str)
	{
		return addTooltip(StatCollector.translateToLocal(str));
	}
	
	//Effectiveness of tool.
	@Override
    public float func_150893_a(ItemStack stack, Block block)
    {
		//A Macuahuitl is a crazy saw sword. Let's do saw things.
		Material mat = block.getMaterial();
		if((mat == Material.plants) ||
			(mat == Material.leaves) ||
			(mat == Material.gourd) ||
			(mat == Material.wood))
		{
			return (efficiency*0.8F);
		}
		return super.func_150893_a(stack, block);
    }
	//Obfuscated name for getting damage vs entity of a non-sword sort.
	@Override
    public float func_150931_i()
    {
		if(toolMaterial != null)
		{
			return toolMaterial.getDamageVsEntity();
		}
		return this.damage;
    }

	/*
	@Override
	public boolean onLeftClickEntity (ItemStack stack, EntityPlayer player, Entity entity)
	{
		//Do the delay thing.
		//if(super.onLeftClickEntity(stack, player, entity))
		//{
		
		if(!(entity instanceof EntityLivingBase)) return false;
		
		EntityLivingBase living = (EntityLivingBase)entity;
		if (living.canAttackWithItem() && (!living.hitByEntity(player))) // Returns true if we CANNOT strike the entity, oddly enough.
		{


			return false;
		}
		return false;
	}*/
	private static void creeperAnger(EntityCreeper creep)
	{
		//Feature disabled for now.
		/*
		if(fuseTimeField == null)
		{
			doCreeperReflection(creep);
		}
		if(fuseTimeField != null)
		{
			try {
				fuseTimeField.setInt(creep, fuseTimeField.getInt(creep)/2);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}*/
	}
	//Only be called when the enemy has actually been struck, and the invulnerability timer has been passed.
    @Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase mob,
			EntityLivingBase player) {
    	
		if(super.hitEntity(stack, mob, player))
		{

			mob.motionX *= knockbackBoost;
			mob.motionZ *= knockbackBoost;
			//Bonus creeper knockback.
			if(mob instanceof EntityCreeper)
			{
				EntityCreeper creep = (EntityCreeper)mob;
				//All of this damage code has been replaced with an onLivingAttack event
				/*
				EntityDamageSource bonus = new EntityDamageSource("player", player);
				creep.setCreeperState(-1); //Defuse the bomb.
				int tempTimer = creep.hurtResistantTime;
				creep.hurtResistantTime = 0;
				creep.attackEntityFrom(bonus, (damage*creeperBonus));
				creep.hurtResistantTime = tempTimer;
				*/	
				creeperAnger(creep);
				
				//TODO: Magic number. Must figure out a saner way to do this.
				mob.motionX *= 1.1;
				mob.motionZ *= 1.1;
			}
			
			mob.hurtResistantTime += 14;
			
			return true;
		}
		return false; 
	}
    
    //TODO: Animate this somehow. 
	@Override
	public ItemStack onItemRightClick(ItemStack ourItem, World world,
			EntityPlayer player) {
		//return super.onItemRightClick(ourItem, world, player);
		//No blocking.
		
		//Sacrifice behavior:
		if((player != null) && (sacrificeBuffs != null) && player.isSneaking())
		{
			//Make sure a player on a half a heart can't suicide with this.
			if(player.getHealth() < 0.6F) return ourItem;
			//Loop through once to make sure we don't have any of these yet.
			for(int i = 0; i < sacrificeBuffs.size(); ++i)
			{
				PotionEffect buff = sacrificeBuffs.get(i);
				//I don't trust contains here. Sorry for the weird tricks.
				//Try-catch is for the potential index out of bounds error.
				try {
					if(player.getActivePotionEffect(Potion.potionTypes[buff.getPotionID()]) != null)
					{
						//We already have one of the effects. Skip all this.
						return ourItem;
					}
				}
				catch(Exception e) { }
			}

			//The rest of this silliness has gone through without stopping. Now, deal damage to the player.
			//TODO: Verify that this always bypasses armor.
			player.attackEntityFrom(sacrificeDamage, player.getHealth()/2);
			//MUST come before adding the buffs, otherwise we'll resist our own damage.
			
			//Loop through again to add 'em.
			for(int i = 0; i < sacrificeBuffs.size(); ++i)
			{
				PotionEffect buff = sacrificeBuffs.get(i);
				player.addPotionEffect(new PotionEffect(buff)); //Copy it, to be safe.
			}

			//Bone mac repair behavior.
			if(this.toolMaterial.toString().equalsIgnoreCase("BONE"))
			{
				//TODO: Remove magic numbers, add static fields. Configurability, maybe?
				int toRepair = ourItem.getItemDamage() / 2;
				ourItem.setItemDamage(ourItem.getItemDamage() - toRepair); 
				//TODO: Test setItemDamage to make sure it doesn't desync (do we need a packet for this?)
			}
			else
			{
				//Small damage to tool
				ourItem.attemptDamageItem(1, this.itemRand);
			}
			
		}
		return ourItem;
	}
	/**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
	@Override
    public Multimap getItemAttributeModifiers()
    {
        Multimap multimap = super.getItemAttributeModifiers();
        //Undo what we just did in super.getItemAttributeModifiers();
        multimap.remove(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)this.swordDmg, 0));
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)this.damage, 0));
        return multimap;
    }
	
	public float getDamage() {
		return damage;
	}
	public ItemMacuahuitl setDamage(float damage) {
		this.damage = damage;
		return this;
	}
	public float getKnockbackBoost() {
		return knockbackBoost;
	}
	public ItemMacuahuitl setKnockbackBoost(float knockbackBoost) {
		this.knockbackBoost = knockbackBoost;
		return this;
	}

	public float getCreeperBonus() {
		return creeperBonus;
	}
	public ItemMacuahuitl setCreeperBonus(float creeperBonus) {
		this.creeperBonus = creeperBonus;
		return this;
	}
	
	public ItemMacuahuitl addBuff(PotionEffect eff)
	{
		//Initialize the list
		if(sacrificeBuffs == null)
		{
			sacrificeBuffs = new ArrayList<PotionEffect>(3);
		}
		for(int i = 0; i < sacrificeBuffs.size(); ++i)
		{
			//No duplicates.
			if(sacrificeBuffs.get(i).getPotionID() == eff.getPotionID()) return this; 
		}
		sacrificeBuffs.add(eff);
		return this;
	}
}
