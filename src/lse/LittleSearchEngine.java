package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		if(docFile == null) throw new FileNotFoundException("File Not Found");
		HashMap<String, Occurrence> hashmap = new HashMap<String, Occurrence>();
		Scanner sc = new Scanner(new File(docFile));
		while(sc.hasNext()){
			String word = getKeyword(sc.next());
			if(word!=null){
				if(hashmap.containsKey(word)){// duplicate of word; increase frequency
					hashmap.get(word).frequency++;
				}
				else{
					hashmap.put(word, new Occurrence(docFile, 1));
				}
			}
		}
		return hashmap;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for(String k : kws.keySet()){
			if(keywordsIndex.containsKey(k)){
				keywordsIndex.get(k).add(kws.get(k));
				insertLastOccurrence(keywordsIndex.get(k));
			} else{
				ArrayList<Occurrence> occ = new ArrayList<Occurrence>();
				occ.add(kws.get(k));
				keywordsIndex.put(k, occ);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		ArrayList<Character> punct = new ArrayList<>(Arrays.asList(new Character[]{'.', ',', '?', ':', ';', '!'}));
		
		for(int i = word.length() - 1; i >= 0; i--) {
			if(!punct.contains(word.charAt(i))) { 
				word = word.substring(0, i+1); //include this letter, since it is not punc.
				break;
			}
		}
		
		word = word.toLowerCase();
		if(noiseWords.contains(word) || word.length() == 0) { //if noise word, return null.
			return null;
		}
		
		for(int i = 0; i < word.length(); i++) {
			char letter = word.charAt(i);
			if(!Character.isAlphabetic(letter)) {
				return null;
			}
		}
		
		return word;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		/** COMPLETE THIS METHOD **/
		if(occs.size() ==1) return null;
		ArrayList<Integer> arr = new ArrayList<Integer>();
		Occurrence target = occs.get(occs.size() - 1);//target element
		int lo = 0;
		int hi = occs.size()-2; //excludes target from binary search
		int mid = (lo+hi)/2;
		while(lo<=hi){
			mid = (lo+hi)/2;
			arr.add(mid);//adds middle to array
			if(target.frequency== occs.get(mid).frequency){
				break; //base case
			} else if(target.frequency < occs.get(mid).frequency){
				lo = mid+1;
			} else{
				hi = mid-1;
			}
		}
		occs.add(mid+1,occs.remove(occs.size()-1));
		if (hi < lo) occs.add(lo,occs.remove(occs.size() - 1));
		return arr;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
	
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
//this will hold the documents in which either kw1 or kw2 occurs
ArrayList<String> count5 = new ArrayList<String>();
		
//only five are allowed, we loop until numOfDocuments == five
int total = count5.size();

//first check if the keyword is in the keywordsIndex
if (keywordsIndex.containsKey(kw1) == false && keywordsIndex.containsKey(kw2) == false) return null;

ArrayList<Occurrence> kw1list = null;
ArrayList<Occurrence> kw2list = null;

//we need to get the occurrence of kw1 and kw2
if (keywordsIndex.containsKey(kw1) == true) {
	kw1list = keywordsIndex.get(kw1);
}

if (keywordsIndex.containsKey(kw2) == true) {
	kw2list = keywordsIndex.get(kw2);
}


//this will see if one of the arraylists of occurrences is null
if (kw2list == null) {
	for (int i = 0; i < kw1list.size(); i++) {
		count5.add(kw1list.get(i).document);
		total++;
		if (total == 5) break;
	}
}
else if (kw1list == null) {
	for (int j = 0; j < kw2list.size(); j++) {
		count5.add(kw2list.get(j).document);
		total++;
		if (total == 5) break;
	}
}
else {
	int j = 0;
	int k = 0;
	
	while (j<kw1list.size() && k<kw2list.size()) {
		int num1 = kw1list.get(j).frequency;
		int num2 = kw2list.get(k).frequency;
		if (num1 >= num2) {
			if (count5.contains(kw1list.get(j).document)) {
				j++;
			}
			else {
				count5.add(kw1list.get(j).document);
				total++;
				j++;
			}
		}
		else {
			if (count5.contains(kw2list.get(k).document)) {
				k++;
			}
			else {
				count5.add(kw2list.get(k).document);
				total++;
				k++;
			}
		}
		if (total == 5) break;
	}
	
	if (total < 5) {
		if (j <kw1list.size()) {
			while (total < 5 && j < kw1list.size()) {
				
				if (count5.contains(kw1list.get(j).document)) {
					j++;
				}
				else {
					count5.add(kw1list.get(j).document);
					total++;
					j++;
				}
			}
		}
		else {
			while (total < 5 && k<kw2list.size()) {
				
				if (count5.contains(kw2list.get(k).document)) {
					k++;
				}
				else {
					count5.add(kw2list.get(k).document);
					total++;
					k++;
				}
			}
		}
	}
}
return count5;
}

}	
