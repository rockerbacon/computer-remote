package com.lab309.time;

import android.os.SystemClock;
import android.content.SharedPreferences;

/**
 * Classe para contagem de tempo utilizando SystemClock.elapsedRealtime()
 * Caso deseje-se manter cronometro ativo mesmo quando aplicacao nao esta ativa, deve-se cria-lo com um id unico e utilizar as classes disponiveis para salvar estados do cronometro
 * Uma funcao que receba um TimeUnit trabalhara com a unidade de tempo passada nesse parametro
 * Caso uma funcao que trabalhe com tempo nao receba um TimeUnit essa funcao trabalhara com o tempo em milisegundos
 * Created by Vitor Andrade dos Santos on 11/25/16.
 */

public class Chronometer {

	/*ATTRIBUTES*/
	private long beginning;
	private String beginningFileId;
	private String elapsedFileId;

	/*CONSTRUCTORS*/
	//parametro id serve para salvar informacoes do chronometro para uso caso a aplicacao seja fechada, passe null caso so pretenda usar o cronometro pelo tempo que a aplicacao estiver ativa
	//parametro beginning indica referencia de tempo a partir do qual cronometro deve contar o tempo
	public Chronometer (String id, long beginning) {
		if (id != null) {
			this.setId(id);
		} else {
			this.beginningFileId = null;
			this.elapsedFileId = null;
		}
		this.beginning = beginning;
	}
	public Chronometer (long beginning) {
		this(null, beginning);
	}
	public Chronometer () {
		this(null, SystemClock.elapsedRealtime());
	}

	/*
	 * instanciar cronometro a partir de dados salvos em um SharedPreferences
	 * se parametro check eh false verificacao de corretude e existencia dos dados em file nao eh feita
	 */
	public Chronometer (String id, SharedPreferences file, boolean check) {

		this.setId(id);
		if (check) {
			if (file.contains(this.beginningFileId)) {
				long beginningTime = file.getLong(this.beginningFileId, 0x7FFFFFFFFFFFFFFFl);
				long totalTime = file.getLong(this.elapsedFileId, 0l);

				if (SystemClock.elapsedRealtime() - beginningTime < totalTime) {
					this.beginning = -totalTime;
				} else {
					this.beginning = beginningTime;
				}
			} else {
				this.beginning = SystemClock.elapsedRealtime();
			}
		} else {
			this.beginning = file.getLong(this.beginningFileId, 0l);
		}
	}

	/*SETTERS*/
	private void setId (String id) {
		this.beginningFileId = id + ".beginning";
		this.elapsedFileId = id + ".elapsed";
	}

	/*METHODS*/
	//retorna tempo decorrido
	public long getElapsed () {
		return SystemClock.elapsedRealtime() - this.beginning;
	}
	public double getElapsed (TimeUnit unit) {
		return (SystemClock.elapsedRealtime() - this.beginning) / unit.conversionDivisor;
	}

	//reinicia contagem de tempo
	public void reset () {
		this.beginning = SystemClock.elapsedRealtime();
	}

	//retorna tempo decorrido e reseta contagem
	public long count () {
		long time = this.beginning;
		this.beginning = SystemClock.elapsedRealtime();
		return this.beginning - time;
	}
	public double count (TimeUnit unit) {
		long time = this.beginning;
		this.beginning = SystemClock.elapsedRealtime();
		return (this.beginning - time) / unit.conversionDivisor;
	}

	//retorna verdadeiro se o tempo passado como parametro ja passou
	public boolean elapsed (long time) {
		if (SystemClock.elapsedRealtime() - this.beginning < time) {
			return false;
		}
		return true;
	}
	public boolean elapsed (double time, TimeUnit unit) {
		if ( SystemClock.elapsedRealtime() - this.beginning < time * unit.conversionDivisor ) {
			return false;
		}
		return true;
	}

	//reinicia contagem de tempo se o tempo especificado passou, retornando quantas vezes esse tempo passou
	public long countElapsed (long time) {
		long actual = SystemClock.elapsedRealtime();
		long passed = actual - this.beginning;
		if (passed < time) {
			return 0;
		}
		this.beginning = actual;
		return passed/time;
	}
	public long countElapsed (double time, TimeUnit unit) {
		return this.countElapsed((long)(time*unit.conversionDivisor));
	}

	public void saveTo (SharedPreferences file) {
		SharedPreferences.Editor editor = file.edit();

		editor.putLong(this.beginningFileId, this.beginning);
		editor.putLong(this.elapsedFileId, this.getElapsed());
		editor.apply();
	}


}