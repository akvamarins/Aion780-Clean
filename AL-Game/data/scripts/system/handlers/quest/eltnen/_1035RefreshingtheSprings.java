/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import java.util.Collections;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002 -- TODO Timer and Fail dialog when implemented
 * 
 */
public class _1035RefreshingtheSprings extends QuestHandler
{
	private final static int	questId	= 1035;
	private final static int[]	npc_ids	= { 203917, 203992, 700158, 203965, 203968, 203987, 700160, 203934, 700159 };

	public _1035RefreshingtheSprings()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		for(int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED  || player.getCommonData().getLevel() < 26)
			return false;

		qs.setStatus(QuestStatus.START);
		updateQuestStatus(env);
		return true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203917)
				return sendQuestEndDialog(env);
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 203917)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 0)
						return sendQuestDialog(env, 1011);
					else if(var == 4)
						return sendQuestDialog(env, 1352);					
					return false;
				case SETPRO1:
					if(var == 0)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
				case SETPRO2:
					if(var == 4)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
				return false;					
			}
		}
		else if(targetId == 203992)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 1)
						return sendQuestDialog(env, 1352);
					else if(var == 3)
						return sendQuestDialog(env, 1693);						
				case SETPRO2:
					if(var == 1)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
				case SETPRO3:
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}					
				return false;
			}
		}
		else if(targetId == 700158 && var == 2)
		{
			if (env.getDialog() == DialogAction.USE_OBJECT && player.getInventory().getItemCountByItemId(182201014) == 1)
			{
				final int targetObjectId = env.getVisibleObject().getObjectId();
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
						player.getInventory().decreaseByItemId(182201014, 1);
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						updateQuestStatus(env);						
					}
				}, 3000);
			}
			return false;
		}		
		else if(targetId == 203965)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 4)
						return sendQuestDialog(env, 2034);
				case SETPRO4:
					if(var == 4)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);						
						return true;
					}
				return false;
			}
		}
		else if(targetId == 203968)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 5)
						return sendQuestDialog(env, 2375);
				case SETPRO5:
					if(var == 5)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
				return false;
			}
		}
		else if(targetId == 203987)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 6)
						return sendQuestDialog(env, 2716);
					else if(var == 8)
						return sendQuestDialog(env, 3057);			
				case SETPRO6:
					if(var == 6)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						giveQuestItem(env, 182201024, 1);							
						return true;
					}
				case SETPRO7:
					if(var == 8)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						giveQuestItem(env, 182201025, 1);						
						return true;
					}					
				return false;
			}
		}
		else if(targetId == 700160 && var == 7)
		{
			if (env.getDialog() == DialogAction.USE_OBJECT && player.getInventory().getItemCountByItemId(182201024) == 1)
			{
				final int targetObjectId = env.getVisibleObject().getObjectId();
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
						player.getInventory().decreaseByItemId(182201024, 1);
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						updateQuestStatus(env);						
					}
				}, 3000);
			}
			return false;
		}		
		else if(targetId == 203934)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 9)
						return sendQuestDialog(env, 3398);
					else if(var == 11)
						return sendQuestDialog(env, 3739);						
				case SETPRO8:
					if(var == 9)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
					}
					else if(var == 11)
					{
						closeDialogWindow(env);					
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return true;
					}						
				return false;
			}		
		}
		else if(targetId == 700159 && var == 10)
		{
			if (env.getDialog() == DialogAction.USE_OBJECT && player.getInventory().getItemCountByItemId(182201025) == 1)
			{
				final int targetObjectId = env.getVisibleObject().getObjectId();
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, 0, targetObjectId), true);
						player.getInventory().decreaseByItemId(182201025, 1);
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						updateQuestStatus(env);						
					}
				}, 3000);
			}
			return false;
		}		
		return false;
	}
}
