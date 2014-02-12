package main;
import java.util.Random;
/**
* Fornisce metodi statici di ordinamento per array di interi.
* 
*/
public class SortUtils {
	
	private static Random gen=new Random();

	/**
	* Restituisce una copia ordinata di un array di interi, utilizzando l' algoritmo merge-sort.
	* Utilizza un ulteriore array d' appoggio della stessa dimensione di quello originale.
	*
	* @param a	l' array da ordinare
	* @return	una copia ordinata di a
	*/
	public static int[] mergeSort(int[] a){
		if (a==null) throw new NullPointerException();
		int[] copy= new int[a.length];
		int[] result= new int[a.length];
		for(int i=0; i<a.length; ++i) copy[i]=a[i];
		if (copy.length==1) return copy;
		recMergeSort(copy,result,0,copy.length-1);
		return result;
	}
	
	private static void recMergeSort(int[] a, int[] b, int sx, int dx){
		int cx=0;
		if (sx<dx){
			cx = (sx+dx)/2;
			recMergeSort(a, b, sx, cx);
			recMergeSort(a, b, cx+1, dx);
			fusion(a, b, sx, cx, dx);
		}
	}
	
	private static void fusion(int[] a, int[] b, int sx, int cx, int dx){
		int i=sx, j=cx+1, k=sx;
		while(i<=cx && j<=dx){
			if (a[i]<a[j]){ b[k]=a[i]; ++i; ++k;}
			else {b[k]=a[j]; ++j; ++k;}
		}
		while(i<=cx){b[k]=a[i]; ++i; ++k;}
		while(j<=dx){b[k]=a[j]; ++j; ++k;}
		for(i= sx; i<=dx; ++i) a[i]=b[i];
	}
	
	/**
	* Returns a sorted copy of an integer array , using quick-sort algorithm.
	*
	* @param a - original array
	* @return - an sorted copy of a
	*/
	public static int[] quickSort(int[] a){
		if (a==null) throw new NullPointerException();
		int[] copy= new int[a.length];
		for(int i=0; i<a.length; ++i) copy[i]=a[i];
		recQuickSort(copy, 0, copy.length-1);
		return copy;
	}
	
	/**
	 * Sort the integer array a from sx index to dx index included, using quick-sort algorithm.
	 * 
	 * @param a - the array to be sorted
	 * @param sx - minimum index for sorting
	 * @param dx - maximum index for sorting
	 */
	public static void recQuickSort(int[] a, int sx, int dx){
		if(sx<dx){
			int pivot=sx+((gen.nextInt(1000000)& 0x7FFFFFFF)%(1+(dx-sx)));
			int perno=distribute(a, sx, pivot, dx);
			recQuickSort(a, sx, perno-1);
			recQuickSort(a, perno+1, dx);
		}		
	}

	private static int distribute(int[] a, int sx, int pivot, int dx){
		if(pivot!=dx) exchange(a, pivot, dx);
		int i=sx, j=dx-1;
		while(i<j){
			while(i<j && a[i]<=a[dx]) ++i;
			while(i<j && a[j]>=a[dx]) --j;
			if(i<j) exchange(a, i, j);
		}
		if(i!=dx-1){ exchange(a, i, dx); return i;}
		else if(a[i]<=a[dx]) return dx;
		else{ exchange(a, i, dx); return i;}
	}
	
	private static void exchange(int[] a, int i, int j){
		int tmp=a[i];
		a[i]=a[j];
		a[j]=tmp;
	}
}