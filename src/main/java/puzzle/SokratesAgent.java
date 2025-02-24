package puzzle;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;


import jadex.bdiv3.BDIAgentFactory;
import jadex.bdiv3.annotation.BDIConfiguration;
import jadex.bdiv3.annotation.BDIConfigurations;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalAPLBuild;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanFailed;
import jadex.bdiv3.annotation.PlanPassed;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.IPlan;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.annotation.Agent;

/**
 *  Puzzle agent tries to solve a checkers game
 *  by recursiveky applying means-end-reasoning.
 */
@Agent(type=BDIAgentFactory.TYPE)
@BDIConfigurations(
{	
	@BDIConfiguration(name=MoveComparator.STRATEGY_PREFER_CAPTURES), // Best strategy first.
	@BDIConfiguration(name=MoveComparator.STRATEGY_PREFER_QUEENS),
	@BDIConfiguration(name=MoveComparator.STRATEGY_PREFER_QUEENS_THAN_PREFER_CAPTURES),
	@BDIConfiguration(name=MoveComparator.STRATEGY_PREFER_NO_QUEENS_THAN_PREFER_CAPTURES),
	@BDIConfiguration(name=MoveComparator.STRATEGY_PREFER_NO_QUEENS), 
	@BDIConfiguration(name=MoveComparator.STRATEGY_NONE)
})
public class SokratesAgent
{
	//-------- attributes --------
	
	/** The puzzle board. */
	@Belief
	protected IBoard	board	= JackBoard.getInstance();
	
	/** The number of tried moves. */
	@Belief // needs not to be belief, just used here to test the BDIDebugger
	protected int	triescnt;
	
	/** The depth of the current move. */
	protected int	depth;
	
	/** The delay between two moves (in milliseconds). */
	protected long delay	= 800;
	
	/** The strategy */
	protected String strategy; // = MoveComparator.STRATEGY_PREFER_CAPTURES;
	
	protected int playerPieceColor;

	//-------- methods --------
	
	/**
	 *  Setup the gui and start playing.
	 */
	//@AgentBody
	@OnStart
	public IFuture<Void>	body(IInternalAccess agent)
	{
		// Agent select it's color to play
		this.playerPieceColor = board.getPlayerPieceColor();

		final Future<Void>	ret	= new Future<Void>();
		strategy = agent.getConfiguration();
		print("strategy is: "+strategy);
		Random rand = new Random();
		this.delay = delay + rand.nextInt(500);
		print(this.delay + " delay");
		if (this.playerPieceColor == 1) {
			createGui(agent);
		}

		print("Now puzzling:");
		final long	start	= System.currentTimeMillis();
		IFuture<MoveGoal> fut = agent.getFeature(IBDIAgentFeature.class).dispatchTopLevelGoal(new MoveGoal());
		fut.addResultListener(new IResultListener<MoveGoal>()
		{
			public void resultAvailable(MoveGoal movegoal)
			{
				long end = System.currentTimeMillis();
				System.out.println("Needed: "+(end-start)+" millis.");
				ret.setResult(null);
			}
			
			public void exceptionOccurred(Exception exception)
			{
				System.out.println("No solution found :-(");
				ret.setResult(null);
			}
		});
		
		return ret;
	}
	
	/**
	 *  Create the GUI (if any).
	 */
	protected void	createGui(final IInternalAccess agent)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new BoardGui(agent.getExternalAccess(), board);
			}
		});
	}
	
	//-------- goals --------
	
	/**
	 *  The goal to make moves until the board reaches a solution.
	 */
	@Goal
	public class MoveGoal
	{
		/**
		 *  Move goal is successful when resulting board represents a solution.
		 */
		@GoalTargetCondition//(beliefs="board")
		public boolean	isAchieved()
		{
			return board.isSolution();
		}
		
		/**
		 *  Build plan candidates for all possible moves.
		 *  Sorts moves according to strategy.
		 */
		@GoalAPLBuild
		public List<MovePlan> buildAPL()
		{
			List<MovePlan>	ret	= new ArrayList<MovePlan>();
			List<Move>	moves	= board.getPossibleMoves(playerPieceColor);
			print("Possible moves: "+moves);
			Collections.sort(moves, new MoveComparator(board, strategy));
			
			for(Move move: moves)
			{
				ret.add(new MovePlan(move));
			}

			return ret;
		}
	}
	
	//-------- plans --------
	
	/**
	 *  Plan to make a move.
	 */
	@Plan(trigger=@Trigger(goals=MoveGoal.class))
	public class MovePlan
	{
		//-------- attributes --------
		
		/** The move. */
		protected Move move;
		
		//-------- constructors --------
		
		/**
		 *  Create a move plan-
		 */
		public MovePlan(Move move)
		{
			this.move = move;
		}
		
		//-------- methods --------
		
		/**
		 *  The plan body.
		 */
		@PlanBody
		public IFuture<Void>	move(final IPlan plan)
		{
			final Future<Void>	ret	= new Future<Void>();
			plan.waitFor(delay>1000?2000:delay);

			triescnt++;
			print("Trying "+move+" ("+triescnt+") ", depth);
			depth++;
			boolean success = board.move(move);
			if (!success) {
				fail();
			}
			if(delay>0)
			{
				plan.waitFor(delay)
					.addResultListener(new DelegationResultListener<Void>(ret)
				{
					public void customResultAvailable(Void result)
					{
						IFuture<MoveGoal> fut = plan.dispatchSubgoal(new MoveGoal());
						fut.addResultListener(new ExceptionDelegationResultListener<MoveGoal, Void>(ret)
						{
							public void customResultAvailable(MoveGoal result)
							{
								ret.setResult(null);
							}
						});
					}
				});
			}
			else
			{
				IFuture<MoveGoal> fut = plan.dispatchSubgoal(new MoveGoal());
				fut.addResultListener(new ExceptionDelegationResultListener<MoveGoal, Void>(ret)
				{
					public void customResultAvailable(MoveGoal result)
					{
						ret.setResult(null);
					}
				});
			}
			
			return ret;
		}
		
		/**
		 *  The plan failure code.
		 */
		@PlanFailed
		public IFuture<Void> failed(IPlan plan)
		{
			print("Failed");
			assert board.getLastMove().equals(move): "Tries to takeback wrong move.";
			
			Future<Void>	ret	= new Future<Void>();
			
			depth--;
			print("Failed "+move, depth);
			board.takeback();
			if(delay>0)
			{
				plan.waitFor(delay).addResultListener(new DelegationResultListener<Void>(ret));
			}
			else
			{
				ret.setResult(null);
			}
			
			return ret;
		}

		/**
		 *  The plan passed code.
		 */
		@PlanPassed
		public void passed()
		{
			depth--;
			print("Succeeded "+move, depth);
		}
	}


	/**
	 *  Print out an indented string.
	 *  @param text The text.
	 *  @param indent The number of cols to indent.
	 */
	protected void print(String text, int indent)
    {
        for(int x=0; x<indent; x++)
            System.out.print(" ");
        System.out.println(playerPieceColor+" "+text);
    }

	protected void print(String text)
		{
			print(text, 0);
		}
}
