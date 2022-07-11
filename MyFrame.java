import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import java.util.Random;

import javax.swing.*;

public class MyFrame extends JFrame implements KeyListener{
	public static final int SCREEN_WIDTH=1000;
	public static final int SCREEN_HEIGHT=600;
	ImageIcon rocketImage=new ImageIcon("rocket.png");
	ImageIcon ufoImage=new ImageIcon("ufo.png");
	ImageIcon shootImage=new ImageIcon("shoot_final.png");
	ImageIcon burgerImage=new ImageIcon("burger.png");
	boolean[] isPressed= new boolean[4]; //0=w 1=a 2=s 3=d
	JLabel rocketLabel;
	Timer timer;
	Timer timerUfoGenerator;
	Timer timerShoots;
	Timer timerBurger;
	Timer timerNotShootingSince; //serve a fare andare avanti notShootingSince. Potevo anche farlo dentro timer, cioè il timer principale, ma viene male ci sono problemi
	int notShootingSince=0; //serve a fare in modo che non ci sia delay iniziale se non serve,quando inizio a sparare.Conta i ms dall'ultimo sparo
	List<JLabel> ufosList=new LinkedList<>();
	List<JLabel> shootsList=new LinkedList<>();
	List<JLabel> burgersList=new LinkedList<>();
	Random random=new Random();
	JLabel pointsLabel=new JLabel("0");
	int points=0;
	boolean shooting=false;
	int[] shootingDelay= {1000,900,800,700,600,500,400};
	
	MyFrame(){
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);
		this.setLayout(null);
		this.setIconImage(rocketImage.getImage());
		this.getContentPane().setBackground(Color.black);
		this.setTitle("Rocket Game");
		this.setLocationRelativeTo(null);
		isPressed[0]=false;
		isPressed[1]=false;
		isPressed[2]=false;
		isPressed[3]=false;
		rocketLabel=new JLabel();
		rocketLabel.setIcon(rocketImage);
		rocketLabel.setBounds(200, 200, 150, 150);
		pointsLabel.setBounds(50, SCREEN_HEIGHT-100, 100, 40);
		pointsLabel.setForeground(Color.green);
		pointsLabel.setFont(new Font("MV Boli",Font.PLAIN,20));
		this.add(pointsLabel);				
		timer=new Timer(10,e->{
			move();
			checkCollisionShootUfo();
			if(checkCollisionRocketUfo() || rocketLabel.getX()<-200 || rocketLabel.getX()>SCREEN_WIDTH+50
					|| rocketLabel.getY()<-200 || rocketLabel.getY()>SCREEN_HEIGHT +50) {
				gameOver();
			}
			checkCollisionRocketBurger();	
		});
		timer.start();
		
		timerUfoGenerator=new Timer(500,e->{
			if(points<8000 || ( (int )points/ (int) 1000)%2==1 ){
				if(!this.getBackground().equals(Color.black)) {
					this.getContentPane().setBackground(Color.black);
				}
				generateUfo();
				refreshPoints();
			}else {
				if(!this.getBackground().equals(new Color(128, 22, 23))){
					this.getContentPane().setBackground(new Color(128, 22, 23));
				}
				generateUfoWhereRocketIs();
				refreshPoints();
			}
		});
		timerUfoGenerator.start();
		timerShoots=new Timer(shootingDelay[0],e->{
			if(shooting) {
				generateShoot();
			}
		if(points <14000) { //dopo 14k finisco i delay possibili, lascio il piu veloce
			timerShoots.setDelay(shootingDelay[ ((int) points/ (int) 2000) ]);
			timerShoots.setInitialDelay(shootingDelay[ ((int) points/ (int) 2000) ]);
			timerShoots.restart();
		}
		});
		timerShoots.start();
		timerBurger=new Timer(10000,e->{
			generateBurger();
			
		});
		timerBurger.start();
		timerNotShootingSince=new Timer(10,e->{
			if(!shooting) {
				notShootingSince+=10;
			}else {
				notShootingSince=0;
			}
			
		});
		timerNotShootingSince.start();
		this.addKeyListener(this);
		this.add(rocketLabel);
		this.setVisible(true);
		
	
	}
	

	private int increasingDifficultUfosSpeed() {
		int x= (int) points/ (int)500;
		return x==0?1:x;
	}

	public void move() {
		if(isPressed[0]) {
			rocketLabel.setLocation(rocketLabel.getX(), rocketLabel.getY()-5);
		}
		if(isPressed[1]) {
			rocketLabel.setLocation(rocketLabel.getX()-5,rocketLabel.getY());
		}
		if(isPressed[2]) {
			rocketLabel.setLocation(rocketLabel.getX(), rocketLabel.getY()+5);
		}
		if(isPressed[3]) {
			rocketLabel.setLocation(rocketLabel.getX()+5, rocketLabel.getY());
		}
		ufosList.stream().forEach(u->{
			u.setLocation(u.getX(), u.getY()+increasingDifficultUfosSpeed());
		});
		shootsList.stream().forEach(s->{
			s.setLocation(s.getX(),s.getY()-5 -2*((int) points/ (int) 2000));
		});
		ufosList=ufosList.stream().filter(u->{
			return u.getY()<=SCREEN_HEIGHT;
		}).collect(Collectors.toList());
		shootsList=shootsList.stream().filter(s->{
			return s.getY()<=SCREEN_HEIGHT;
		}).collect(Collectors.toList());
		burgersList.stream().forEach(b->{
			b.setLocation(b.getX() + 2, b.getY()+10);
		});
	}
	
	public void generateUfo() {
		int x=random.nextInt(SCREEN_WIDTH);
		JLabel ufoLabel=new JLabel();
		ufoLabel.setIcon(ufoImage);
		ufoLabel.setBounds(x, -10, 120, 120);
		ufosList.add(ufoLabel);
		this.add(ufoLabel);
		
	}
	
	private void generateUfoWhereRocketIs() {
		int x=rocketLabel.getX();
		JLabel ufoLabel=new JLabel();
		ufoLabel.setIcon(ufoImage);
		ufoLabel.setBounds(x, -10, 120, 120);
		ufosList.add(ufoLabel);
		this.add(ufoLabel);
		
	}
	public void generateShoot() {
		int x=rocketLabel.getX()+60; //il +60 sistema il disallineamento
		int y=rocketLabel.getY()-50; //sparo a partire da sopra la navicella
		JLabel shootLabel=new JLabel();
		shootLabel.setIcon(shootImage);
		shootLabel.setBounds(x,y,50,50);
		shootsList.add(shootLabel);
		this.add(shootLabel);
		
	}
	
	public void generateBurger() {
		int x=random.nextInt(SCREEN_WIDTH);
		JLabel burgerLabel=new JLabel();
		burgerLabel.setIcon(burgerImage);
		burgerLabel.setBounds(x, -10, 60, 60);
		burgersList.add(burgerLabel);
		this.add(burgerLabel);
	}
	
	
	public boolean checkCollisionRocketUfo() {
		long col=ufosList.stream().filter(u->{
			int x=rocketLabel.getX();
			int y=rocketLabel.getY();
			int xUfo=u.getX();
			int yUfo=u.getY();
			return (x>xUfo-50 && x<xUfo+50 && y>yUfo-50 && y<yUfo+50);
		}).count();
		return col>0;
	}
	
	public void checkCollisionShootUfo(){
		List<JLabel> hittedUfos=new LinkedList<>();
		List<JLabel> hittingShoots=new LinkedList<>();
		ufosList.stream().filter( (JLabel u)->{
				long c=shootsList.stream().filter(s->{
				int xUfo=u.getX();
				int yUfo=u.getY();
				int xShoot=s.getX();
				int yShoot=s.getY();
				return (xUfo>xShoot-100 && xUfo<xShoot+50 && yUfo>yShoot-50 && yUfo<yShoot+50);
			}).peek(s->{
				hittingShoots.add(s);
				this.remove(s);
				}).count();
				points+=100*c; //aggiungo 100 punti per ogni ufo distrutto
			return c>0;
		}).forEach(u->{
			hittedUfos.add(u);
			this.remove(u);
		});
		hittingShoots.stream().forEach(s->shootsList.remove(s));
		hittedUfos.stream().forEach(u->ufosList.remove(u));
//		if(hittingShoots.size() >0 ||hittedUfos.size()<0 ) {
//			System.out.println(hittingShoots.size() +"" + hittedUfos.size());
//		}
		this.repaint();
	}
	
	public void checkCollisionRocketBurger() {
		List<JLabel> hittedBurgers= new LinkedList<>();
		burgersList.stream().filter(b->{
				int x=rocketLabel.getX();
				int y=rocketLabel.getY();
				int xBurger=b.getX();
				int yBurger=b.getY();
				return (x>xBurger-80 && x<xBurger+80 && y>yBurger-80 && y<yBurger+80);
		}).forEach(b->hittedBurgers.add(b));
		hittedBurgers.stream().forEach(b->{
			this.remove(b);
			burgersList.remove(b);
			points+=1000;
		});
	}
	
	
	public void refreshPoints() {
		points+=10;
		pointsLabel.setText(points+"");
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//System.out.println(" KEY TYPED typed char: "+e.getKeyChar());
		
		switch (e.getKeyChar()) {
		case 'w':
			isPressed[0]=true;
			
			break;
		case 'a':
			isPressed[1]=true;
			
			break;
		case 's':
			isPressed[2]=true;
		
		break;
		case 'd':
			isPressed[3]=true;
		
		break;
		
		
			
			
		}
		
		
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//System.out.println("KEY PRESSED typed code of key: "+ e.getKeyCode());
		if(e.getKeyCode()==32) {
			shooting=true;
		//	System.out.println(notShootingSince+" "+ timerShoots.getDelay()+"");
			if(notShootingSince>=timerShoots.getDelay()) { //serve a evitare il delay iniziale di quando inizio a sparare
				//se ho già aspettato il tempo giusto per caricare l'arma, non ha senso che io aspetti ancora
				generateShoot();
				timerShoots.restart();
			}
			notShootingSince=0;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("KEY RELEASED typed char" +e.getKeyChar()+" with code of key: "+ e.getKeyCode());
		switch (e.getKeyChar()) {
		case 'w':
			isPressed[0]=false;
			
			break;
		case 'a':
			isPressed[1]=false;
			
			break;
		case 's':
			isPressed[2]=false;
		
		break;
		case 'd':
			isPressed[3]=false;
		
		break;
			
			
		}
		
		if(e.getKeyCode()==32) {
			shooting=false;
		}
	}
	
	
	public void gameOver() {
		timer.stop();
		timerUfoGenerator.stop();
		this.getContentPane().removeAll();
		JLabel gameOverLabel=new JLabel("Game Over!");
		//System.out.println(points);
		gameOverLabel.setFont(new Font("MV Boli",Font.BOLD,100));
		gameOverLabel.setForeground(Color.red);
		gameOverLabel.setBounds(200, 100, 800, 200);
		//gameOverLabel.setBackground(Color.green);
		//gameOverLabel.setOpaque(true);
		JLabel finalPointsLabel=new JLabel("Final Score: "+String.valueOf(points));
		finalPointsLabel.setFont(new Font("MV Boli",Font.PLAIN,50));
		finalPointsLabel.setForeground(Color.green);
		finalPointsLabel.setBounds(gameOverLabel.getX()+120,gameOverLabel.getY()+30,800,400);
		this.add(gameOverLabel);
		this.add(finalPointsLabel);
		JButton restartButton=new JButton("Play again");
		restartButton.setBackground(Color.green);
		restartButton.setForeground(Color.white);
		restartButton.setFont(new Font("MV Boli",Font.PLAIN,20));
		restartButton.setBounds(SCREEN_HEIGHT-200, SCREEN_WIDTH/2 -30, 200, 60);
		restartButton.setFocusable(false);
		restartButton.addActionListener(ev->{
			if(ev.getSource().equals(restartButton)) {
				new MyFrame();
				this.dispose();
			}
		});
		this.add(restartButton);
		this.repaint();
	}
	
	

}
