package xadrez;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import tabuleiro.Peca;
import tabuleiro.Posicao;
import tabuleiro.Tabuleiro;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorCor;
	private Tabuleiro tabuleiro;
	private boolean check;
	
	private List<Peca> pecasTabuleiro = new ArrayList<>();
	private List<Peca> pecasCapturadas = new ArrayList<>();
	
	public PartidaXadrez() {
		tabuleiro = new Tabuleiro(8, 8);
		turno = 1;
		jogadorCor = Cor.BRANCO;
		setupInicial();
	}
	
	public int getTurno() {
		return turno;
	}

	public Cor getJogadorCor() {
		return jogadorCor;
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public PecaXadrez[][] getPecas(){
		PecaXadrez[][] mat = new PecaXadrez[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for(int i = 0; i<tabuleiro.getLinhas(); i++) {
			for(int j = 0; j<tabuleiro.getColunas(); j++) {
				mat[i][j] = (PecaXadrez) tabuleiro.peca(i, j);
			}
		}
		return mat;
	}
	
	public boolean[][] movimentosPossiveis(PosicaoXadrez posicaoOrigem){
		Posicao posicao = posicaoOrigem.toPosicao();
		validacaoPosicaoOrigem(posicao);
		return tabuleiro.peca(posicao).movimentosPossiveis();
	}
	
	public PecaXadrez executarMovimentoXadrez(PosicaoXadrez posicaoOrigem, PosicaoXadrez posicaoDestino) {
		Posicao origem = posicaoOrigem.toPosicao();
		Posicao destino = posicaoDestino.toPosicao();
		
		validacaoPosicaoOrigem(origem);
		validacaoPosicaoDestino(origem, destino);
		
		Peca pecaCapturada = realizarMovimento(origem, destino);
		
		if(testCheck(jogadorCor)) {
			desfazerMovimento(origem, destino, pecaCapturada);
			throw new XadrezException("Você não pode se colocar em check");
		}
		
		check = (testCheck(oponente(jogadorCor))) ? true : false;
		
		proximoTurno();
		return (PecaXadrez)pecaCapturada;
	}
	
	private Peca realizarMovimento(Posicao origem, Posicao destino) {
		Peca p = tabuleiro.removePeca(origem);
		Peca pecaCapturada = tabuleiro.removePeca(destino);
		tabuleiro.PosicaoPeca(p, destino);
		
		if(pecaCapturada != null) {
			pecasTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}
		
		return pecaCapturada;
	}
	
	private void desfazerMovimento(Posicao origem, Posicao destino, Peca pecaCapturada) {
		Peca p = tabuleiro.removePeca(destino);
		tabuleiro.PosicaoPeca(p, origem);
		
		if (pecaCapturada != null) {
			tabuleiro.PosicaoPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasTabuleiro.add(pecaCapturada);
		}
	}
	
	private void validacaoPosicaoOrigem(Posicao posicao) {
		if(!tabuleiro.existePeca(posicao)) {
			throw new XadrezException("Não existe peça na posição selecionada");
		}
		if(jogadorCor != ((PecaXadrez)tabuleiro.peca(posicao)).getCor()) {
			throw new XadrezException("A peça escolhida não é sua");
		}
		if(!tabuleiro.peca(posicao).temMovimentoPossivel()) {
			throw new XadrezException("Não existe movimentos possiveis para a peça escolhida");
		}
	}
	
	private void validacaoPosicaoDestino(Posicao origem, Posicao destino) {
		if (!tabuleiro.peca(origem).movimentosPossiveis(destino)) {
			throw new XadrezException("A peça escolhida não pode se mover para a posição escolhida");
		}
	}
	
	private void proximoTurno() {
		turno++;
		jogadorCor = (jogadorCor == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}
	
	private Cor oponente(Cor cor) {
		return (cor == cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}
	
	private PecaXadrez rei(Cor cor) {
		List<Peca> lista = pecasTabuleiro.stream().filter(x -> ((PecaXadrez)x).getCor() == cor).collect(Collectors.toList());
		for (Peca p : lista) {
			if(p instanceof Rei) {
				return (PecaXadrez)p;
			}
		}
		throw new IllegalStateException("Não existe o rei " + cor + " no tabuleiro");
	}
	
	private boolean testCheck(Cor cor) {
		Posicao reiPosicao = rei(cor).getPosicaoXadrez().toPosicao();
		List<Peca> oponentePeca = pecasTabuleiro.stream().filter(x -> ((PecaXadrez)x).getCor() == oponente(cor)).collect(Collectors.toList());
		for (Peca p : oponentePeca) {
			boolean[][] mat = p.movimentosPossiveis();
			if(mat[reiPosicao.getLinha()][reiPosicao.getColuna()]) {
				return true;
			}
		}
		return false;
	}
	
	private void novoLugarPeca(char coluna, int linha, PecaXadrez peca) {
		tabuleiro.PosicaoPeca(peca, new PosicaoXadrez(coluna, linha).toPosicao());
		pecasTabuleiro.add(peca);
	}
	
	private void  setupInicial() {
		novoLugarPeca('c', 1, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('c', 2, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('d', 2, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('e', 2, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('e', 1, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('d', 1, new Rei(tabuleiro, Cor.BRANCO));

		novoLugarPeca('c', 7, new Torre(tabuleiro, Cor.PRETO));
		novoLugarPeca('c', 8, new Torre(tabuleiro, Cor.PRETO));
		novoLugarPeca('d', 7, new Torre(tabuleiro, Cor.PRETO));
		novoLugarPeca('e', 7, new Torre(tabuleiro, Cor.PRETO));
		novoLugarPeca('e', 8, new Torre(tabuleiro, Cor.PRETO));
		novoLugarPeca('d', 8, new Rei(tabuleiro, Cor.PRETO));
	}
}
