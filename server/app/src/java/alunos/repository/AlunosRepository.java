package alunos.repository;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.Hasher;
import utils.autenticador.Autenticador;
import utils.autenticador.Cargos;


public class AlunosRepository {
	private Connection c;
	
	public AlunosRepository(Connection c){
		this.c = c;
	}
	
	public boolean cadastrar(String email, String senha, String nome, String idMunicipio, String idUf) throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException, ParseException{
		String hashedSenha = Hasher.hash(senha);
		
		Boolean assinante = false;
		
		PreparedStatement ps = c.prepareStatement("INSERT INTO aluno (\"email-aluno\", senha, nome, \"id-municipio\", \"id-uf\", assinante) VALUES (?, ?, ?, ?, ?, ?)");
		ps.setString(1, email);
		ps.setString(2, hashedSenha);
		ps.setString(3, nome);
		ps.setInt(4, Integer.parseInt(idMunicipio));
		ps.setInt(5, Integer.parseInt(idUf));
		ps.setBoolean(6, assinante);
		
		return ps.executeUpdate() != 0;
	}
	
	public boolean logar(HttpServletRequest req, HttpServletResponse res, String email, String senha) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException{
		Autenticador aut = new Autenticador(req, res);
		
		PreparedStatement pt = c.prepareStatement("SELECT * FROM aluno WHERE \"email-aluno\" = ?");
		pt.setString(1, email);
		ResultSet rs = pt.executeQuery();
		rs.next();
		Long idLong = rs.getLong("id-aluno");
		
		if (Hasher.validar(senha, rs.getString("senha"))) {
			aut.logar(idLong, Cargos.ALUNO, false);
			return true;
		}
		return false;
	}
	
	public boolean editar(String idAluno, String nome, String idMunicipio, String idUf, String preferenciaPreco, String preferenciaLocal, String preferenciaNumeroAlunos, String assinante, String dataFimAssinatura) throws SQLException, ParseException {
		int adcs = 0;
		int cont = 1;
		boolean[] pars = new boolean[8];
		
		
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		Date dataUtil = null;
		java.sql.Date dataSql = null;
		
		for (int i = 0; i < 8; i++) {
			pars[i] = false;
		}
		
		
		String query = "UPDATE aluno SET";
		if (!"".equals(nome)) {
			query += " nome= ?";
			adcs++;
			pars[0] = true;
		}
		if (!"".equals(idMunicipio)) {
			if (adcs > 0) {
				query += ",";
			}
			query += " \"id-municipio\"= ?";
			adcs++;
			pars[1] = true;
		}
		if (!"".equals(idUf)) {
			if (adcs > 0) {
				query += ",";
			}
			query += " \"id-uf\"= ?";
			adcs++;
			pars[2] = true;
		}
		if (!"".equals(preferenciaPreco)) {
			if (adcs > 0) {
				query += ",";
			}
			query += " \"preferencia-preco\"= ?";
			adcs++;
			pars[3] = true;
		}
		if (!"".equals(preferenciaLocal)) {
			if (adcs > 0) {
				query += ",";
			}
			query += " \"id-preferencia-local\"= ?";
			adcs++;
			pars[4] = true;
		}
		if (!"".equals(preferenciaNumeroAlunos)) {
			if (adcs > 0) {
				query += ",";
			}
			query += " \"preferencia-numero-alunos\"= ?";
			adcs++;
			pars[5] = true;
		}
		if (!"".equals(assinante)) {
			if (adcs > 0) {
				query += ",";
			}
			query += " assinante= ?";
			adcs++;
			pars[6] = true;
		}
		if (!"".equals(dataFimAssinatura)) {
			dataUtil = formato.parse(dataFimAssinatura);
			dataSql = new java.sql.Date(dataUtil.getTime());
			if (adcs > 0) {
				query += ",";
			}
			query += " \"data-fim-assinatura\"= ?";
			pars[7] = true;
		}
		

		query += " WHERE \"id-aluno\" = ?";

		
		
		PreparedStatement ps = c.prepareStatement(query);

		
		if (pars[0]) {
			ps.setString(cont, nome);
			cont++;
		}
		if (pars[1]) {
			ps.setInt(cont, Integer.parseUnsignedInt(idMunicipio));
			cont++;
		}
		if (pars[2]) {
			ps.setInt(cont, Integer.parseUnsignedInt(idUf));
			cont++;
		}
		if (pars[3]) {
			ps.setFloat(cont, Float.parseFloat(preferenciaPreco));
			cont++;
		}
		if (pars[4]) {
			ps.setInt(cont, Integer.parseInt(preferenciaLocal));
			cont++;
		}
		if (pars[5]) {
			ps.setInt(cont, Integer.parseInt(preferenciaNumeroAlunos));
			cont++;
		}
		if (pars[6]) {
			ps.setBoolean(cont, Boolean.parseBoolean(assinante));
			cont++;
		}
		if (pars[7]) {
			ps.setDate(cont, dataSql);
			cont++;
		}
		

		ps.setLong(cont, Long.parseLong(idAluno));

		int sucesso = ps.executeUpdate();

		return sucesso != 0;
	}
	
	public boolean alterarSenha(Long id, String senha) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {		
		String hashSenha = null;
		hashSenha = Hasher.hash(senha);
		String query = "UPDATE aluno SET senha= ? WHERE \"id-aluno\" = ?";

		PreparedStatement ps = c.prepareStatement(query);
		ps.setString(1, hashSenha);
		ps.setLong(2, id);
		int sucesso = ps.executeUpdate();
		return sucesso != 0;
	}
	
	
}