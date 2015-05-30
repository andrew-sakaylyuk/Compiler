import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
 
public class Form extends JFrame {
	private static final long serialVersionUID = 1L;

	public Form() {
 
        super("Yip-Yip Compiler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        Font font = new Font("Verdana", Font.PLAIN, 10);
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(font);
 
        JTextArea inText = new JTextArea();
        JScrollPane in = new JScrollPane(inText);
        JTextArea outText = new JTextArea();
        outText.setEditable(false);
        JScrollPane out = new JScrollPane(outText);
        tabbedPane.addTab("Сирцевий код", in);
        tabbedPane.addTab("Асемблерний код", out);
        
        
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        
        JPanel buttons = new JPanel();
        content.add(buttons, BorderLayout.PAGE_END);
 
        JButton run = new JButton("Скомпілювати");
        URL url = getClass().getResource("run.png");        
        Image im = Toolkit.getDefaultToolkit().getImage(url);
        run.setIcon(new ImageIcon(im));
        run.setPreferredSize(new Dimension(165,30));
        run.setFont(new Font("Verdana", Font.BOLD, 11));
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	Parser parse = null;
				try { 
					Parser.label = 0;
					Lexer.line = 1;
					outText.setText("Виникла помилка!");
					parse = new Parser(inText.getText(), outText);
					parse.program();
					if (Parser.err == false) tabbedPane.setSelectedIndex(1);
					Parser.err = false;
				} catch (IOException e1) {} 
            }
        });
        buttons.add(run);
        
        UIManager.put("FileChooser.openButtonText", "Відкрити");
    	UIManager.put("FileChooser.cancelButtonText", "Відмінити");
    	UIManager.put("FileChooser.saveButtonText", "Зберегти");
    	UIManager.put("FileChooser.saveButtonToolTipText", "Зберегти");
    	UIManager.put("FileChooser.openButtonToolTipText", "Відкрити");
    	UIManager.put("FileChooser.cancelButtonToolTipText", "Відмінити");
    	UIManager.put("FileChooser.lookInLabelText", "Шукати в");
    	UIManager.put("FileChooser.lookInLabelText", "Папка");
    	UIManager.put("FileChooser.saveInLabelText", "Папка");
    	UIManager.put("FileChooser.fileNameLabelText", "Назва файлу");
    	UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлу");
    	UIManager.put("FileChooser.upFolderToolTipText", "На один рівень вгору");
    	UIManager.put("FileChooser.newFolderToolTipText", "Створити нову папку");
    	UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
    	UIManager.put("FileChooser.detailsViewButtonToolTipText", "Таблиця");
    	UIManager.put("FileChooser.fileNameHeaderText", "Назва");
    	UIManager.put("FileChooser.fileSizeHeaderText", "Розмір");
    	UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
    	UIManager.put("FileChooser.fileDateHeaderText", "Змінений");
    	UIManager.put("FileChooser.fileAttrHeaderText", "Атрибути");
    	UIManager.put("FileChooser.acceptAllFileFilterText", "Всі файли");
        
        JButton load = new JButton("Відкрити");
        url = getClass().getResource("load.png");        
        im = Toolkit.getDefaultToolkit().getImage(url);
        load.setIcon(new ImageIcon(im));
        load.setPreferredSize(new Dimension(165,30));
        load.setFont(new Font("Verdana", Font.BOLD, 11));
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JFileChooser fileOpen = new JFileChooser();
            	int ret = fileOpen.showDialog(null,"Відкрити"); 
            	if (ret == JFileChooser.APPROVE_OPTION) {
            	    BufferedReader b;
					try {
						String str = "";
						b = new BufferedReader(new FileReader(fileOpen.getSelectedFile()));
						while(b.ready()) str += b.readLine() + "\n"; 
						inText.setText(str);
					} catch (Exception e1) { }
                    
            	}
            }
        });
        buttons.add(load);
        
        JButton save = new JButton("Зберегти");
        url = getClass().getResource("save.png");        
        im = Toolkit.getDefaultToolkit().getImage(url);
        save.setIcon(new ImageIcon(im));
        save.setPreferredSize(new Dimension(165,30));
        save.setFont(new Font("Verdana", Font.BOLD, 11));
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JFileChooser fileSave = new JFileChooser();
                if ( fileSave.showDialog(null, "Зберегти") == JFileChooser.APPROVE_OPTION ) {
                    try ( FileWriter fw = new FileWriter(fileSave.getSelectedFile()) ) {
                        fw.write(outText.getText());
                    }
                    catch ( IOException e1 ) {  }
                }
            }
        });
        buttons.add(save);
        
        content.add(tabbedPane, BorderLayout.CENTER);
 
        getContentPane().add(content);
 
        setPreferredSize(new Dimension(600,400));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
 
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame.setDefaultLookAndFeelDecorated(true);
                new Form();
            }
        });
    }
}
