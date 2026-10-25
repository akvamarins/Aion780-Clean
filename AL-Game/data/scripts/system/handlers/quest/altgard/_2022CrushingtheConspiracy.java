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

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author HGabor85
 *
 */
public class _2022CrushingtheConspiracy extends QuestHandler
{
	private final static int	questId	= 2022;

	public _2022CrushingtheConspiracy()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(203557).addOnTalkEvent(questId);
		qe.registerQuestNpc(700140).addOnTalkEvent(questId);
		qe.registerQuestNpc(700142).addOnTalkEvent(questId);
		qe.registerQuestNpc(210753).addOnKillEvent(questId);
		qe.registerQuestNpc(700141).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
		{
			if(targetId == 203557)
			{
				if(env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			switch(targetId)
			{
				case 203557:
				{
					if (qs.getQuestVarById(0) == 0)
					{
						if(env.getDialog() == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1352);
						else if(env.getDialog() == DialogAction.SELECT_QUEST_REWARD)
						{
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							closeDialogWindow(env);
							
							return true;
						}
					}
				}
				break;
				case 700140:
				{
					if (qs.getQuestVarById(0) == 1)
					{
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						updateQuestStatus(env);
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(320030000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 320030000, newInstance.getInstanceId(), 275.68f, 164.03f, 205.19f);
						closeDialogWindow(env);
						return true;
					}
				}
				break;
				case 700142:
				{
					if (qs.getQuestVarById(0) == 2)
					{
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}

				}
				break;
				case 700141:
				{
					if (qs.getQuestVarById(0) == 4)
					{
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						TeleportService2.teleportTo(player, 220030000, 2453.0f, 2553.2f, 316.3f, (byte) 26, TeleportAnimation.BEAM_ANIMATION);
						closeDialogWindow(env);
						return true;
					}
				}
				}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203557)
				return sendQuestEndDialog(env);
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() != QuestStatus.START)
			return false;
		switch(targetId)
		{
			case 210753:
				if(var >= 3 && var < 4)
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					return true;
				}
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
                int[] quests = {2200, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021};
                for (int id : quests)
                {
                        QuestState qs2 = player.getQuestStateList().getQuestState(id);
                        if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
                                return false;
                }

                qs.setStatus(QuestStatus.START);
                updateQuestStatus(env);
                return true;
        }
}
