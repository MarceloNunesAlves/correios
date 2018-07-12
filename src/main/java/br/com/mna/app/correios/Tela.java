package br.com.mna.app.correios;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;

public class Tela extends JFrame {

	private JPanel contentPane;
	private JTextField txtRastreio;
	private JTextField txtCEP;
	private JLabel lblSucesso;
	private Connection conn;
	private JLabel lblObjetoJGravado;

	public void createDerby() throws SQLException, ClassNotFoundException {
		// -------------------------------------------
		// URL format is
		// jdbc:derby:<local directory to save data>
		// -------------------------------------------
		boolean criarTabela = false;
		File dir = new File("dados");
		if (!dir.isDirectory()) {
			criarTabela = true;
		}
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		String dbUrl = "jdbc:derby:dados;create=true";
		conn = DriverManager.getConnection(dbUrl);

		if (criarTabela) {
			Statement stmt = conn.createStatement();

			// drop table
			// stmt.executeUpdate("Drop Table users");
			// create table
			stmt.executeUpdate("Create table objeto (cd_rastreio varchar(15) primary key, cep varchar(9))");
		}
	}

	public void insertObj(String rastreio, String cep) throws SQLException {
		Statement stmt = conn.createStatement();

		stmt.executeUpdate("insert into objeto values ('" + rastreio + "','" + cep + "')");
	}

	public void delete() throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("delete from objeto");
	}

	public boolean getRastreio(String codigo) throws SQLException {
		Statement stmt = conn.createStatement();
		// query
		ResultSet rs = stmt.executeQuery("SELECT * FROM objeto where cd_rastreio='" + codigo + "'");

		if (rs.next()) {
			// codigo encontrado
			return false;
		} else {
			// proximo campo
			return true;
		}
	}

	public void report() throws SQLException {
		Statement stmt = conn.createStatement();
		// query
		ResultSet rs = stmt.executeQuery("SELECT * FROM objeto");
		// print out query result
		String conteudo = "Objeto;cep\n";
		while (rs.next()) {
			conteudo += rs.getString("cd_rastreio") + ";" + rs.getString("cep") + "\n";
		}

		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			// save to file
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(conteudo);

			} catch (IOException e) {
			} finally {
				try {
					if (writer != null)
						writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public Tela() {
		setTitle("Controle de objetos - Correios");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 559, 419);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("Código de rastreio:");
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		lblNewLabel.setBounds(36, 19, 197, 34);
		contentPane.add(lblNewLabel);

		txtRastreio = new JTextField();
		txtRastreio.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10 || e.getKeyCode() == 13) {
					try {
						if (getRastreio(txtRastreio.getText())) {
							lblObjetoJGravado.setText("");
							txtCEP.requestFocus();
						} else {
							lblObjetoJGravado.setText("Objeto já gravado - " + txtRastreio.getText());
							txtRastreio.setText("");
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		txtRastreio.setFont(new Font("Lucida Grande", Font.PLAIN, 32));
		txtRastreio.setBounds(36, 65, 483, 58);
		contentPane.add(txtRastreio);
		txtRastreio.setColumns(10);

		txtCEP = new JTextField();
		txtCEP.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10 || e.getKeyCode() == 13) {
					try {
						insertObj(txtRastreio.getText(), txtCEP.getText());
						txtRastreio.setText("");
						txtCEP.setText("");
						lblSucesso.setText("Item gravado com sucesso");

						txtRastreio.requestFocus();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		txtCEP.setFont(new Font("Lucida Grande", Font.PLAIN, 32));
		txtCEP.setColumns(10);
		txtCEP.setBounds(36, 210, 483, 58);
		contentPane.add(txtCEP);

		JLabel lblCep = new JLabel("CEP:");
		lblCep.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		lblCep.setBounds(36, 164, 197, 34);
		contentPane.add(lblCep);

		JButton btnReport = new JButton("Gerar relatório");
		btnReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					report();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnReport.setBounds(33, 346, 162, 29);
		contentPane.add(btnReport);

		JButton btnClean = new JButton("Limpar tabela");
		btnClean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 int dialogButton = JOptionPane.YES_NO_OPTION;
			    JOptionPane.showConfirmDialog (null, "Deseja realmente excluir todos os registros?","Warning",dialogButton);
			    if (dialogButton == JOptionPane.YES_OPTION) {
			    	try {
						delete();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    }    
			}
		});
		btnClean.setBounds(357, 346, 162, 29);
		contentPane.add(btnClean);

		lblSucesso = new JLabel("");
		lblSucesso.setForeground(Color.BLUE);
		lblSucesso.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		lblSucesso.setBounds(147, 280, 260, 41);
		contentPane.add(lblSucesso);

		lblObjetoJGravado = new JLabel("");
		lblObjetoJGravado.setForeground(Color.RED);
		lblObjetoJGravado.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		lblObjetoJGravado.setBounds(36, 120, 483, 41);
		contentPane.add(lblObjetoJGravado);
		
		try {
			createDerby();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
