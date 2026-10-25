/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package quest.altgard;

import java.util.Collections;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Mr. Poke
 *
 */
public class _2016FearThis extends QuestHandler
{

	private final static int	questId	= 2016;

	public _2016FearThis()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(203631).addOnTalkEvent(questId);
		qe.registerQuestNpc(210455).addOnKillEvent(questId);
		qe.registerQuestNpc(210458).addOnKillEvent(questId);
		qe.registerQuestNpc(214032).addOnKillEvent(questId);
		qe.registerQuestNpc(203621).addOnTalkEvent(questId);
		qe.registerQuestItem(182203019, questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		final int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() == QuestStatus.START)
		{
			switch (targetId)
			{
				case 203631:
					switch(env.getDialog())
					{
						case QUEST_SELECT:
							if(var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 6)
								return sendQuestDialog(env, 1352);
						case SETPRO1:
						case SETPRO2:
							if (var == 0 || var == 6)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
					}
					break;
				case 203621:
					switch(env.getDialog())
					{
						case QUEST_SELECT:
							if(var == 7)
								return sendQuestDialog(env, 1693);
							else if (var == 8)
								return sendQuestDialog(env, 2034);
							break;
						case SETPRO3:
						case SETPRO4:
							if (var == 7 || var == 9)
							{
								if (var == 9)
									if (!giveQuestItem(env, 182203019, 1))
										return true;
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
							break;
						case CHECK_USER_HAS_QUEST_ITEM:
							if(var == 8)
							{
								if(QuestService.collectItemCheck(env, true))
								{
									qs.setQuestVarById(0, var + 1);
									updateQuestStatus(env);
									return sendQuestDialog(env, 2035);
								}
								else
									return sendQuestDialog(env, 2120);
							}
					}
					
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203631)
			{
				if (env.getDialog() == DialogAction.USE_OBJECT )
					return sendQuestDialog(env, 2375);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if(id != 182203019)
			return HandlerResult.FAILED;
		if(!player.isInsideZone(ZoneName.get("Q2016")))
			return HandlerResult.FAILED;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return HandlerResult.FAILED;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				player.getInventory().decreaseByObjectId(itemObjId, 1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
		}, 3000);
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;


		int targetId = 0;
		int var = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		switch(targetId)
		{
			case 210455:
			case 210458:
			case 214032:
				var = qs.getQuestVarById(0);
				if (var < 6)
				{
					qs.setQuestVarById(0, var+1);
					updateQuestStatus(env);
				}
				break;
		}
		return false;
	}
	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED)
			return false;
		qs.setStatus(QuestStatus.START);
		updateQuestStatus(env);
		return true;
	}
}
