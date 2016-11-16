//// FILE HEADER
//                   
// Main Class File:  PokemonGO.java
// File:             Pokedex.java
// Semester:         Fall 2016
//
// Author:           Rouyi Ding
// CS Login:         rouyi
// TA's Name:        Leah Kluegel
//
//
// Credits:          none
////////////////////////////////////////////////////////////////////////////

import java.util.Iterator;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Scanner;

/**
 * The Pokedex maintains the list of Pokemon species that have been encountered
 * by the trainer.It also maintains the trainer's inventory of Pokemon belonging
 * to a particular species
 * 
 * Bugs: none known
 *
 * @author       Rouyi Ding
 * @version      1.0
 */

public class Pokedex {
	private ArrayList<PokemonSpecies> pokedex;

	/**
	 * Create a new, empty Pokedex
	 */
	public Pokedex() {
		pokedex = new ArrayList<PokemonSpecies>();
	}

  /**
   * Load Pokedex from a file and update the Pokedex object's private pokedex.
   * <p>
   * This function does not provide de-duplication (if Gengar is already in 
   * your Pokedex and you loadFromFile a Pokedex that also has a Gengar, you 
   * will have two entries of Gengar in your Pokedex). An error is thrown to
   *  prevent duplicate entries.
   *  
   * @param filepath the Pokedex to load; the file is expected to have lines 
   * delimited by white space with the following fields: 
   *   <Pokedex Number> <Species Name> <Candies> [<cp1>, <cp2>, ...]
   * for example:
   *   94 gengar 3 2289
   *   110 weezing 6 457 148
   * @throws PokedexException if the pokedex has already been loaded (pokedex
   *  is not empty) with message Config.MULTIPLE_POKEDEX_EXCEPTION
   * @throws FileNotFoundException if the file in @filepath@ cannot be found
   */
	public void loadFromFile(String filepath) 
			throws FileNotFoundException, PokedexException {
		if (!pokedex.isEmpty())
			throw new PokedexException(Config.MULTIPLE_POKEDEX_EXCEPTION);

		File file = new File(filepath);
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			String[] tokens = line.split("\\s+");
			int pokedexNumber = Integer.parseInt(tokens[0]);
			String specName = tokens[1].toLowerCase();

			int candies = 0;
			if (tokens.length >= 3) {
				candies = Integer.parseInt(tokens[2]);
			}
			PokemonSpecies tempSpec = 
					new PokemonSpecies(pokedexNumber, specName, candies);
			for (int i = 3; i < tokens.length; i++) {
				Pokemon temp =
			            new Pokemon(pokedexNumber, specName,
			            		Integer.parseInt(tokens[i]));
				tempSpec.loadPokemon(temp);
			}
			this.pokedex.add(tempSpec);
		}
		sc.close();
	}

	/**
	 * Create a record of a new Pokemon species in the Pokedex; used by
	 * PokemonTrainer when the player encounters a new Pokemon and that Pokemon
	 * escapes. This method is also used by other Pokedex methods such as
	 * addNewPokemon to make record of any encountered Pokemon (whether that
	 * Pokemon is captured or escapes)
	 *
	 * @param species the PokemonSpecies that has been encountered
	 */
	public void addNewSpecies(PokemonSpecies species) {
		try {
			this.findSeenSpeciesData(species.getSpeciesName());
			// then it has been seen before, do nothing
		} catch (PokedexException e) {
			// then Pokemon has not been seen, add it
			pokedex.add(species);
		}
	}

	/**
	 * Add a new Pokemon to the player's inventory; may add a new species to the
	 * Pokedex if the Pokemon being added has not been encountered before. Used
	 * by PokemonTrainer when the player encounters and catches a Pokemon.
	 *
	 * @param pokemon the new Pokemon that has been caught
	 */
	public void addNewPokemon(Pokemon pokemon) {
		// TODO
		try {
			// if it is not a new pokemon species, add a new Pokemon to the
			// player's inventory
			PokemonSpecies temp = 
					this.findSeenSpeciesData(pokemon.getSpecies());
			temp.addNewPokemon(pokemon);
			// TODO: increase candies
		} catch (PokedexException e) {
			// if it is a new pokemon species, add a new species to the Pokedex
			// and add a new Pokemon to the player's inventory
			// int pokedexNumber, String speciesName, int candies
			PokemonSpecies newSpec =
					new PokemonSpecies(pokemon.getPokedexNumber(),
							pokemon.getSpecies(), 0);
			pokedex.add(newSpec);
			newSpec.addNewPokemon(pokemon);
		}
	}

  /**
   * Transfer a Pokemon to the Professor. The Professor thanks the player by 
   * providing a candy associated with the Pokemon species of the Pokemon that
   *  was transferred.
   *
   * @param speciesName the species of the {@link Pokemon} to transfer
   * @param cp the combatPower of the {@link Pokemon} to transfer
   * @return the pokemon that was tranferred
   * @throws PokedexException when either the Pokemon species given by the 
   * species name has not
   * yet been encountered by the player or if there is no Pokemon with the 
   * combatPower given in cp
   */
	public Pokemon transferPokemon(String speciesName, int cp) 
			throws PokedexException {
		// throws PokedexException when either the Pokemon species given by the
		// species name has not yet been encountered by the player
		PokemonSpecies hasSeenSpecies = this.findSeenSpeciesData(speciesName);
		// throws PokedexException when there is no Pokemon with the combatPower
		// given in cp
		return hasSeenSpecies.transferPokemon(cp);
	}

	
  /**
   * Lookup a species in the Pokedex; if it has been seen before, return its 
   * data; otherwise, throw PokedexException.
   *
   * @param name the species name to lookup in the Pokedex
   * @return the PokemonSpecies with speciesName given by name
   * @throws PokedexException if the PokemonSpecies cannot be found (the player 
   * has not yet encountered this species)
   */
  public PokemonSpecies findSeenSpeciesData(String name)
		  throws PokedexException {
	  Iterator<PokemonSpecies> iterator = this.pokedex.iterator();
	  while (iterator.hasNext()){
		  PokemonSpecies temp = iterator.next();
		  if (temp.getSpeciesName().equals(name)) {
				return temp;
			}
	  }
		throw new PokedexException(String.format(Config.UNSEEN_POKEMON, name));
	}

  /**
   * While {@link findSeenSpeciesData} returns information for all Pokemon 
   * observed (and caught), this function only returns species data for Pokemon
   * that the Trainer has in his or her inventory.
   *
   * @param speciesName the name of the species to find in the Pokedex
   * @throws PokedexException [Config.UNSEEN_POKEMON] if the species has not 
   * been encountered or [Config.UNCAUGHT_POKEMON] if the player does not have 
   * any Pokemon of that species in his or her inventory
   */
	public PokemonSpecies findCaughtSpeciesData(String speciesName)
			throws PokedexException {
		PokemonSpecies seenSpecies = findSeenSpeciesData(speciesName);
		if (seenSpecies.isEmpty()) {
			throw new PokedexException(
					String.format(Config.UNCAUGHT_POKEMON, speciesName));
		} else {
			return seenSpecies;
		}
	}

  	/**
	 * Create a String with the encountered Pokemon in the form: Bulbasaur
	 * Charmander ... where Pokemon species are listed in the order they have
	 * been encountered
	 *
	 * @return the String as described above
	 */
  public String seenPokemonMenu() {
	  String encounteredPoke = "";
	  
	  for (int i=0; i<this.pokedex.size(); i++){
		  encounteredPoke = encounteredPoke +
				  this.pokedex.get(i).getSpeciesName()+"\n";
	  }
	  return encounteredPoke;
  }

	/**
	 * While {@link seenPokemonMenu} returns a String with all Pokemon in the
	 * Pokedex (even ones that the trainer failed to capture), this function
	 * returns a String containing only Pokemon current in the trainer's
	 * inventory
	 *
	 * @return the String as described above
	 */
	public String caughtPokemonMenu() {
		String caughtPokemon = "";
		Iterator<PokemonSpecies> iterator = this.pokedex.iterator();
		while (iterator.hasNext()){
			PokemonSpecies temp = iterator.next();
			if (temp.isEmpty())
				continue;
			caughtPokemon = caughtPokemon +
					temp.getSpeciesName() + "\n";
		}
		return caughtPokemon;
	}

	/**
	 * Serialize Pokedex into a String for a save file. The file format is
	 * described in {@link loadFromFile}: <Pokedex Number> <Species Name>
	 * <Candies> [<cp1>, <cp2>, ...]
	 * 
	 * @return the complete Pokedex in String form, ready to be written to a
	 *         file
	 */
	public String toString() {
		// TODO
		String str = "";
		Iterator<PokemonSpecies> iterator = this.pokedex.iterator();
		while (iterator.hasNext()) {
			str += iterator.next() + "\n";
		}
		return str;
	}
}
