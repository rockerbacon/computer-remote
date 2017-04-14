package com.lab309.time;

import android.content.SharedPreferences;

/**
 * Relogio simples utilizando o tempo desde o ultimo boot
 * Uma funcao que receba um TimeUnit trabalhara com a unidade de tempo passada nesse parametro
 * Caso uma funcao que trabalhe com tempo nao receba um TimeUnit essa funcao trabalhara com o tempo em milisegundos
 * Created by Vitor Andrade dos Santos on 11/25/16.
 */

public class Clock {

	/*ATTRIBUTES*/
	private static final TimeUnit[] unit = TimeUnit.values();

	private int[] time;
	private int lowestUnitIndex;
	private Chronometer chronometer;

	/*CONSTRUCTORS*/
	private Clock (TimeUnit lowestUnit, TimeUnit highestUnit, Chronometer chronometer) {
		this.chronometer = chronometer;
		this.time = new int[highestUnit.ordinal() - lowestUnit.ordinal() + 1];
		this.lowestUnitIndex = lowestUnit.ordinal();
		for (int i : this.time) {
			i = 0;
		}
		this.update();
	}
	/*
	 * parametros lowestUnit e highestUnit determinam a menor e maior unidade de tempo com a qual o relogio opera, respectivamente
	 * um relogio que receba TimeUnit.SECONDS e TimeUnit.DAYS contara segundos, minutos, horas e dias
	 * parametro beginning indica referencia de tempo a partir da qual relogio deve contar
	 */
	public Clock (TimeUnit lowestUnit, TimeUnit highestUnit, long beginning) {
		this(lowestUnit, highestUnit, new Chronometer(beginning));
	}
	public Clock (TimeUnit lowestUnit, TimeUnit highestUnit) {
		this(lowestUnit, highestUnit, new Chronometer());
	}
	//instancicao a partir de dados de um Chronometer salvos em um SharedPreferences
	public Clock (TimeUnit lowestUnit, TimeUnit highestUnit, String id, SharedPreferences file, boolean check) {
		this(lowestUnit, highestUnit, new Chronometer(id, file, check));
	}

	/*GETTERS*/
	//retorna tempo marcado na unidade
	//a tentativa de obter uma unidade nao marcada pelo relogio gerara um excecao
	public int get (TimeUnit unit) {
		return this.time[unit.ordinal() - this.lowestUnitIndex];
	}

	//retorna o tempo decorrido total
	public long getTotalElapsed () {
		return this.chronometer.getElapsed();
	}
	public double getTotalElapsed (TimeUnit unit) {
		return this.chronometer.getElapsed(unit);
	}

	/*METHODS*/
	public void update () {
		int elapsed;
		int i;
		short ceiling;

		i = 0;
		elapsed = (int)this.chronometer.getElapsed(Clock.unit[lowestUnitIndex]);
		while (i < this.time.length - 1 && elapsed > 0) {
			ceiling = Clock.unit[lowestUnitIndex+i].ceiling;
			this.time[i] = elapsed % ceiling;
			elapsed /= ceiling;
			i++;
		}
		this.time[i] = elapsed;

	}

	public void reset () {
		for (int i : this.time) {
			i = 0;
		}
		this.chronometer.reset();
	}

	//retorna verdadeiro se o tempo especificado passou
	public boolean elapsed (long time) {
		return this.chronometer.elapsed(time);
	}
	public boolean elapsed (double time, TimeUnit unit) {
		return this.chronometer.elapsed(time, unit);
	}

	//salvar dados do chronometro para um SharedPreferences
	public void saveTo (SharedPreferences file) {
		this.chronometer.saveTo(file);
	}

}