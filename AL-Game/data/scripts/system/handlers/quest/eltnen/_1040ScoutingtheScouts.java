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

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rhys2002
 * 
 */
public class _1040ScoutingtheScouts extends QuestHandler
{
	private final static int	questId	= 1040;
	
	public _1040ScoutingtheScouts()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(212010).addOnKillEvent(questId);
		qe.registerQuestNpc(204046).addOnKillEvent(questId);
		qe.registerQuestNpc(203989).addOnTalkEvent(questId);
		qe.registerQuestNpc(203901).addOnTalkEvent(questId);
		qe.registerQuestNpc(204020).addOnTalkEvent(questId);
		qe.registerQuestNpc(204024).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED  || player.getCommonData().getLevel() < 31)
			return false;

		qs.setStatus(QuestStatus.START);
		updateQuestStatus(env);
		return true;
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
		if(targetId == 212010)
		{
			if(var > 0 && var < 4)
			{
				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
				updateQuestStatus(env);
				return true;
			}
		}
		else if(targetId == 204046)
		{
			if(var > 7 && var < 9)
			{
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 36));
				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
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
			if(targetId == 203989)
					return sendQuestEndDialog(env);
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 203989)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 0)
						return sendQuestDialog(env, 1011);
					else if(var == 4)
						return sendQuestDialog(env, 1352);					
					return false;
					
				case SELECT_ACTION_1013:
					if(var == 0)
						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 183));
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
		else if(targetId == 203901)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 5)
						return sendQuestDialog(env, 1693);
					return false;

				case SETPRO3:
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
		else if(targetId == 204020)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 6)
						return sendQuestDialog(env, 2034);
					else if(var == 10)
						return sendQuestDialog(env, 3057);						
					return false;

				case SETPRO4:
					if(var == 6)
					{
						TeleportService2.teleportTo(player, 210020000, 2211f, 811f, 513f, (byte) 0, TeleportAnimation.BEAM_ANIMATION);
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						return true;

					}
				case SETPRO7:
					if(var == 10)
					{
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}					
					return false;
			}
		}
		else if(targetId == 204024)
		{
			switch(env.getDialog())
			{
				case QUEST_SELECT:
					if(var == 7)
						return sendQuestDialog(env, 2375);
					else if(var == 9)
						return sendQuestDialog(env, 2716);						
					return false;

				case SETPRO5:
					if(var == 7)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						closeDialogWindow(env);
						return true;
					}
				case SETPRO6:
					if(var == 9)
					{
						TeleportService2.teleportTo(player, 210020000, 1606f, 1529f, 318f, (byte) 0, TeleportAnimation.BEAM_ANIMATION);
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						return true;
					}					
					return false;
			}
		}

		return false;
	}
}
