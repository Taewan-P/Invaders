package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import engine.Cooldown;
import engine.Core;
import engine.GameState;
import engine.Score;

/**
 * Implements the score screen.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class ScoreScreen extends Screen {

	/** Milliseconds between changes in user selection. */
	private static final int SELECTION_TIME = 200;
	/** Maximum number of high scores. */
	private static final int MAX_HIGH_SCORE_NUM = 7;
	/** Code of first mayus character. */
	private static final int FIRST_CHAR = 65;
	/** Code of last mayus character. */
	private static final int LAST_CHAR = 90;

	/** Current player 1 score. */
	private int p1score;
	/** Current player 2 score. */
	private int p2score;
	/** Player 1 lives left. */
	private int p1livesRemaining;
	/** Player 2 lives left. */
	private int p2livesRemaining;
	/** Total bullets shot by the player 1. */
	private int p1bulletsShot;
	/** Total bullets shot by the player 2. */
	private int p2bulletShot;
	/** Total ships destroyed by the player 1. */
	private int p1shipsDestroyed;
	/** Total ships destroyed by the player 2. */
	private int p2shipsDestroyed;
	/** List of past high scores. */
	private List<Score> highScores;
	/** Checks if current score is a new high score. */
	private boolean isNewRecord;
	/** Player 1 name for record input. */
	private char[] p1name;
	/** Player 2 name for record input. */
	private char[] p2name;
	/** Character of player 1's name selected for change. */
	private int p1nameCharSelected;
	/** Character of player 1's name selected for change. */
	private int p2nameCharSelected;
	/** Time between changes in user selection. */
	private Cooldown selectionCooldown;

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 * @param gameState
	 *            Current game state.
	 */
	public ScoreScreen(final int width, final int height, final int fps,
			final GameState gameState) {
		super(width, height, fps);

		this.p1score = gameState.getScore();
		this.p2score = gameState.getScore();
		this.p1livesRemaining = gameState.getLivesRemaining();
		this.p2livesRemaining = gameState.getLivesRemaining();
		this.p1bulletsShot = gameState.getBulletsShot();
		this.p2bulletShot = gameState.getBulletsShot();
		this.p1shipsDestroyed = gameState.getShipsDestroyed();
		this.p2shipsDestroyed = gameState.getShipsDestroyed();
		this.isNewRecord = false;
		this.p1name = "AAA".toCharArray();
		this.p2name = "AAA".toCharArray();
		this.p1nameCharSelected = 0;
		this.p2nameCharSelected = 0;
		this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
		this.selectionCooldown.reset();

		try {
			this.highScores = Core.getFileManager().loadHighScores();
			if (highScores.size() < MAX_HIGH_SCORE_NUM ||
					(highScores.get(highScores.size() - 1).getScore() < this.p1score) ||
					(highScores.get(highScores.size() - 1).getScore() < this.p2score))
				this.isNewRecord = true;

		} catch (IOException e) {
			logger.warning("Couldn't load high scores!");
		}
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		draw();
		if (this.inputDelay.checkFinished()) {
			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				// Return to main menu.
				this.returnCode = 1;
				this.isRunning = false;
				if (this.isNewRecord)
					saveScore();
			} else if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
				// Play again.
				this.returnCode = 2;
				this.isRunning = false;
				if (this.isNewRecord)
					saveScore();
			}

			if (this.isNewRecord && this.selectionCooldown.checkFinished()) {
				if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
					this.p1nameCharSelected = this.p1nameCharSelected == 2 ? 0
							: this.p1nameCharSelected + 1;
					this.selectionCooldown.reset();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
					this.p1nameCharSelected = this.p1nameCharSelected == 0 ? 2
							: this.p1nameCharSelected - 1;
					this.selectionCooldown.reset();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_UP)) {
					this.p1name[this.p1nameCharSelected] =
							(char) (this.p1name[this.p1nameCharSelected]
									== LAST_CHAR ? FIRST_CHAR
							: this.p1name[this.p1nameCharSelected] + 1);
					this.selectionCooldown.reset();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
					this.p1name[this.p1nameCharSelected] =
							(char) (this.p1name[this.p1nameCharSelected]
									== FIRST_CHAR ? LAST_CHAR
							: this.p1name[this.p1nameCharSelected] - 1);
					this.selectionCooldown.reset();
				}
			}
		}

	}

	/**
	 * Saves the score as a high score.
	 */
	private void saveScore() {
		highScores.add(new Score(new String(this.p1name), p1score));
		highScores.add(new Score(new String(this.p2name), p2score));
		Collections.sort(highScores);
		if (highScores.size() > MAX_HIGH_SCORE_NUM)
			highScores.remove(highScores.size() - 1);

		try {
			Core.getFileManager().saveHighScores(highScores);
		} catch (IOException e) {
			logger.warning("Couldn't load high scores!");
		}
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawGameOver(this, this.inputDelay.checkFinished(),
				this.isNewRecord);
		drawManager.drawResults(this, this.p1score, this.p1livesRemaining,
				this.p1shipsDestroyed, (float) this.p1shipsDestroyed
						/ this.p1bulletsShot, this.isNewRecord);

		if (this.isNewRecord)
			drawManager.drawNameInput(this, this.p1name, this.p1nameCharSelected);

		drawManager.completeDrawing(this);
	}
}
