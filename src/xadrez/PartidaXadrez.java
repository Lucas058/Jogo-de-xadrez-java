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
	private boolean checkMate;

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

	public boolean getCheckMate() {
		return checkMate;
	}

	public PecaXadrez[][] getPecas() {
		PecaXadrez[][] mat = new PecaXadrez[tabuleiro.getLinhas()][tabuleiro.getColunas()];
		for (int i = 0; i < tabuleiro.getLinhas(); i++) {
			for (int j = 0; j < tabuleiro.getColunas(); j++) {
				mat[i][j] = (PecaXadrez) tabuleiro.peca(i, j);
			}
		}
		return mat;
	}

	public boolean[][] movimentosPossiveis(PosicaoXadrez posicaoOrigem) {
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

		if (testCheck(jogadorCor)) {
			desfazerMovimento(origem, destino, pecaCapturada);
			throw new XadrezException("Voc� n�o pode se colocar em check");
		}

		check = (testCheck(oponente(jogadorCor))) ? true : false;

		if (testCheckMate(oponente(jogadorCor))) {
			checkMate = true;
		} else {
			proximoTurno();
		}
		return (PecaXadrez) pecaCapturada;
	}

	private Peca realizarMovimento(Posicao origem, Posicao destino) {
		PecaXadrez p = (PecaXadrez)tabuleiro.removePeca(origem);
		p.aumentarContagem();
		Peca pecaCapturada = tabuleiro.removePeca(destino);
		tabuleiro.PosicaoPeca(p, destino);

		if (pecaCapturada != null) {
			pecasTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}

		return pecaCapturada;
	}

	private void desfazerMovimento(Posicao origem, Posicao destino, Peca pecaCapturada) {
		PecaXadrez p = (PecaXadrez)tabuleiro.removePeca(destino);
		tabuleiro.PosicaoPeca(p, origem);
		p.diminuirContagem();
		
		if (pecaCapturada != null) {
			tabuleiro.PosicaoPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasTabuleiro.add(pecaCapturada);
		}
	}

	private void validacaoPosicaoOrigem(Posicao posicao) {
		if (!tabuleiro.existePeca(posicao)) {
			throw new XadrezException("N�o existe pe�a na posi��o selecionada");
		}
		if (jogadorCor != ((PecaXadrez) tabuleiro.peca(posicao)).getCor()) {
			throw new XadrezException("A pe�a escolhida n�o � sua");
		}
		if (!tabuleiro.peca(posicao).temMovimentoPossivel()) {
			throw new XadrezException("N�o existe movimentos possiveis para a pe�a escolhida");
		}
	}

	private void validacaoPosicaoDestino(Posicao origem, Posicao destino) {
		if (!tabuleiro.peca(origem).movimentosPossiveis(destino)) {
			throw new XadrezException("A pe�a escolhida n�o pode se mover para a posi��o escolhida");
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
		List<Peca> lista = pecasTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Peca p : lista) {
			if (p instanceof Rei) {
				return (PecaXadrez) p;
			}
		}
		throw new IllegalStateException("N�o existe o rei " + cor + " no tabuleiro");
	}

	private boolean testCheck(Cor cor) {
		Posicao reiPosicao = rei(cor).getPosicaoXadrez().toPosicao();
		List<Peca> oponentePeca = pecasTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == oponente(cor))
				.collect(Collectors.toList());
		for (Peca p : oponentePeca) {
			boolean[][] mat = p.movimentosPossiveis();
			if (mat[reiPosicao.getLinha()][reiPosicao.getColuna()]) {
				return true;
			}
		}
		return false;
	}

	private boolean testCheckMate(Cor cor) {
		if (!testCheck(cor)) {
			return false;
		}

		List<Peca> lista = pecasTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());
		for (Peca p : lista) {
			boolean[][] mat = p.movimentosPossiveis();
			for (int i = 0; i < tabuleiro.getLinhas(); i++) {
				for (int j = 0; j < tabuleiro.getColunas(); j++) {
					if (mat[i][j]) {
						Posicao origem = ((PecaXadrez) p).getPosicaoXadrez().toPosicao();
						Posicao destino = new Posicao(i, j);
						Peca pecaCapturada = realizarMovimento(origem, destino);
						boolean testCheck = testCheck(cor);
						desfazerMovimento(origem, destino, pecaCapturada);
						if (!testCheck) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	private void novoLugarPeca(char coluna, int linha, PecaXadrez peca) {
		tabuleiro.PosicaoPeca(peca, new PosicaoXadrez(coluna, linha).toPosicao());
		pecasTabuleiro.add(peca);
	}

	private void setupInicial() {
		novoLugarPeca('h', 7, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('d', 1, new Torre(tabuleiro, Cor.BRANCO));
		novoLugarPeca('e', 1, new Rei(tabuleiro, Cor.BRANCO));

		novoLugarPeca('b', 8, new Torre(tabuleiro, Cor.PRETO));
		novoLugarPeca('a', 8, new Rei(tabuleiro, Cor.PRETO));
	}
}
