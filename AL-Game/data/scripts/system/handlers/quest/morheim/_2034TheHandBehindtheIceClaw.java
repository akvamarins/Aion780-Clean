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
package quest.morheim;

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
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002
 * 
 */
public class _2034TheHandBehindtheIceClaw extends QuestHandler
{

	private final static int	questId	= 2034;
	private final static int[]	npc_ids	= { 204303, 204332, 700246, 204301 };

	public _2034TheHandBehindtheIceClaw()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(204417).addOnKillEvent(questId);
		qe.registerQuestNpc(212877).addOnKillEvent(questId);
		for(int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);	 
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED || player.getCommonData().getLevel() < 25)
			return false;

		QuestState qs2 = player.getQuestStateList().getQuestState(2300);
		if(qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
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
			if(targetId == 204301)
			{
				if(env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if(env.getDialog() == DialogAction.SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else return sendQuestEndDialog(env);
			}
			return false;
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 204303)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 0)
						return sendQuestDialog(env, 1011);
					else if(var == 5)
						return sendQuestDialog(env, 2716);						
				case SETPRO1:
					if(var == 0)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
				case SET_SUCCEED:
					if(var == 5)
					{
						qs.setStatus(QuestStatus.REWARD);					
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}					
			}
		}
		else if(targetId == 204332)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 1)
						return sendQuestDialog(env, 1352);
					else if(var == 2)
					{
						if(player.getInventory().getItemCountByItemId(182204008) == 0)
							return sendQuestDialog(env, 1694);
						else 
							return sendQuestDialog(env, 1693);
					}
					else if(var == 3)
						return sendQuestDialog(env, 2034);						
				case SETPRO2:
					if(var == 1)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
					}
						giveQuestItem(env, 182204008, 1);
						closeDialogWindow(env);
						return true;
				case SETPRO4:
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
			}
		}
		else if(targetId == 700246 && player.getInventory().getItemCountByItemId(182204019) >= 1)
		{
			if(env.getDialog() == DialogAction.USE_OBJECT && var == 2)
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
						QuestService.addNewSpawn(220020000, 1, 204417, (float) 461.23,
														(float) 188.82, (float) 485.61, (byte) 25, 30);
						player.getInventory().decreaseByItemId(182204008, 1);								
						player.getInventory().decreaseByItemId(182204019, 1);
					}
				}, 3000);
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;
			
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		
		if(targetId == 204417 && qs.getQuestVarById(0) == 2)
		{
			player.getTitleList().addTitle(58, true, 0);
			qs.setQuestVarById(0, 3);
			updateQuestStatus(env);
		}
		if(targetId == 212877 && qs.getQuestVarById(0) == 4)
		{
			qs.setQuestVarById(0, 5);
			updateQuestStatus(env);
		}		
		return false;
	}	
}
