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
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import professores.model.ProfessorModel;
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
	
	public boolean checarAssinante(Long idAluno) throws SQLException {
		PreparedStatement ps = c.prepareStatement("SELECT assinante from aluno WHERE \"id-aluno\" = ?");
		ps.setLong(1, idAluno);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getBoolean("assinante");
	}
	
	public String consultarPorId(String id) throws SQLException {
		String xml = "<aluno>";
		PreparedStatement ps = c.prepareStatement("SELECT * FROM aluno WHERE \"id-aluno\" = ?");
		Long idParsed = Long.parseLong(id);
		ps.setLong(1, idParsed);
		ResultSet rs = ps.executeQuery();
		rs.next();
		xml += "<id>" + idParsed + "</id>";
		xml += "<nome>" + rs.getString("nome") + "</nome>";
		xml += "<email>" + rs.getString("email-aluno") + "</email>";
		xml += "<id-municipio>" + rs.getInt("id-municipio") + "</id-municipio>";
		xml += "<id-uf>" + rs.getInt("id-uf") + "</id-uf>";
		xml += "<preferencia-preco>" + rs.getFloat("preferencia-preco") + "</preferencia-preco>";
		xml += "<id-preferencia-local>" + rs.getInt("id-preferencia-local") + "</id-preferencia-local>";
		xml += "<preferencia-numero-alunos>" + rs.getInt("preferencia-numero-alunos") + "</preferencia-numero-alunos>";
		xml += "<assinante>" + rs.getBoolean("assinante") + "</assinante>";
		xml += "<data-fim-assinatura>" + rs.getDate("data-fim-assinatura") + "</data-fim-assinatura>";
		
		ps = c.prepareStatement("SELECT * FROM alunopreferenciasmaterias WHERE \"id-aluno\" = ?");
		ps.setLong(1, idParsed);
		rs = ps.executeQuery();
		xml += "<materias>";
		while(rs.next()) {
			xml += "<id-materia>" + rs.getInt("id-materia") + "</id-materia>";
		}
		xml += "</materias>";
		xml += "</aluno>";
		return xml;
	}
	
	public List gerarFeed(double prefPreco, int idPrefLocal, int idMunicipio, int idUf, int prefAlunos, List idMaterias) throws SQLException{
		String idMateriasStr = String.join(",", idMaterias);
		List profs = new LinkedList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		if(idPrefLocal == 1){
			ps = c.prepareStatement("SELECT * FROM professor WHERE \"preco-hora\" < ? AND \"numero-alunos-max\" <= ? AND \"id-municipio\" = ? AND \"id-materia\" IN (" + idMateriasStr + ")");
			ps.setDouble(1, prefPreco);
			ps.setInt(2, prefAlunos);
			ps.setInt(3, idMunicipio);
			
			rs = ps.executeQuery();
		} else if(idPrefLocal == 2){
			ps = c.prepareStatement("SELECT * FROM professor WHERE \"preco-hora\" < ? AND \"numero-alunos-max\" <= ? AND \"id-uf\" = ? AND \"id-materia\" IN (" + idMateriasStr + ")");
			ps.setDouble(1, prefPreco);
			ps.setInt(2, prefAlunos);
			ps.setInt(3, idUf);
			
			rs = ps.executeQuery();
		} else{
			ps = c.prepareStatement("SELECT * FROM professor WHERE \"preco-hora\" < ? AND \"numero-alunos-max\" <= ? AND \"id-materia\" IN (" + idMateriasStr + ")");
			ps.setDouble(1, prefPreco);
			ps.setInt(2, prefAlunos);
			
			rs = ps.executeQuery();
		}
		while(rs.next()){
			ProfessorModel pm = new ProfessorModel(
					rs.getLong("id-prof"),
					rs.getString("email-prof"),
					rs.getString("senha"),
					rs.getString("nome"),
					rs.getString("descricao_apresentacao"),
					rs.getString("titulo_apresentacao"),
					rs.getBoolean("premium"),
					rs.getDouble("avaliacao"),
					rs.getDouble("preco-hora"),
					rs.getInt("numero-avaliacoes"),
					rs.getInt("id-municipio"),
					rs.getInt("id-uf"),
					rs.getInt("id-materia"),
					rs.getInt("numero-alunos-min"),
					rs.getInt("numero-alunos-max"),
					rs.getDate("data-fim-premium")
			);
			profs.add(pm);
		}
		rs.close();
		ps.close();
		return profs;
	}
	
}