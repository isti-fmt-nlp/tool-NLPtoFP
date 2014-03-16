package main;

import java.util.ArrayList;

public class CombinatoryUtils {

	
  /**
   * Calculates all possible combinations of the given values on three positions, <br>
   * where order matters and duplicate values are possible.<br>
   * The order of the elements in the result list will try to put first the lements most different, <br>
   * by considering the combinations as RGB colors <br>
   * (this means that more 'pure' color comes first than less 'pure' ones).
   * 
   * @param values - the int[] containing all possible values
   * @return - a String[] containing all possible combinations in the form of "x.y.z"
   */
  public static String[] threePositionsCombinationsAsStrings(int[] values){
	
//	int[] values={0, 127, 255};
//  int[] values={0, 85, 170, 255};
//  int[] values={0, 64, 128, 192, 255};
//  int[] values={0, 51, 102, 153, 204, 255};
	int index1=0, index2=0, index3=0; 
	ArrayList<String> combinationList= new ArrayList<String>();

	String comb=null;

	//	  PASSI PER CALCOLARE TUTTE LE COMBINAZIONI DI X VALORI SU TRE POSIZIONI:
	//		  [Dove MaxX=255 e MinX è il valore appena sopra 0]

	  
	//step (1)
	//	    1)VALORE SINGOLO
	//	    for(x: da MaxX a MinX){
	//	      -generate all combinations with x, 0, 0;
	//	      -generate all combinations with x, x, 0;
	//	    }
	for(index1=values.length-1; index1>0; --index1){
	  //-generate all combinations with x, 0, 0;
	  comb=values[index1]+".0.0";
	  combinationList.add(comb);

	  comb="0."+values[index1]+".0";
	  combinationList.add(comb);

	  comb="0.0."+values[index1];	
	  combinationList.add(comb);

	  //-generate all combinations with x, x, 0;		
	  comb=values[index1]+"."+values[index1]+".0";
	  combinationList.add(comb);

	  comb=values[index1]+".0."+values[index1];
	  combinationList.add(comb);

	  comb="0."+values[index1]+"."+values[index1];	
	  combinationList.add(comb);
	}

		
	//step (2)
	//	    2)DUE VALORI
	//      for(x: da MaxX a MinX)
	//	      for(y: dal valore sotto MaxX a MinX){
	//		    -generate all combinations with x, y, 0;
	//	      }
	for(index1=values.length-1; index1>0; --index1)
	  for(index2=index1-1; index2>0; --index2){
		//-generate all combinations with x, y, 0;
		comb=values[index1]+"."+values[index2]+".0";
		combinationList.add(comb);

		comb=values[index1]+".0."+values[index2];
		combinationList.add(comb);

		comb=values[index2]+"."+values[index1]+".0";
		combinationList.add(comb);

		comb=values[index2]+".0."+values[index1];			
		combinationList.add(comb);

		comb="0."+values[index1]+"."+values[index2];
		combinationList.add(comb);

		comb="0."+values[index2]+"."+values[index1];		
		combinationList.add(comb);
	  }
		
		
	//step (3)
	//		3)TRE VALORI TUTTI DIVERSI
	//		  for(x: da MaxX a MinX)
	//		    for(y: da MaxX a MinX){
	//		      for(z: da MaxX a MinX){
	//		        -generate all combinations with x, y, z;
	//		  }	
	for(index1=values.length-1; index1>0; --index1)
	  for(index2=index1-1; index2>0; --index2)
		for(index3=index2-1; index3>0; --index3){				
	  	  //-generate all combinations with x, y, z;
		  comb=values[index1]+"."+values[index2]+"."+values[index3];
		  combinationList.add(comb);

		  comb=values[index1]+"."+values[index3]+"."+values[index2];
		  combinationList.add(comb);

		  comb=values[index2]+"."+values[index1]+"."+values[index3];
		  combinationList.add(comb);

		  comb=values[index2]+"."+values[index3]+"."+values[index1];
		  combinationList.add(comb);

		  comb=values[index3]+"."+values[index1]+"."+values[index2];
		  combinationList.add(comb);

		  comb=values[index3]+"."+values[index2]+"."+values[index1];			  
		  combinationList.add(comb);
		}	


	//step (4)
	//	    4)TRE VALORI DI CUI DUE UGUALI E UNO DIVERSO
	//		for(x: da MaxX a MinX)
	//		  for(y: da MaxX a MinX){
	//		    -generate all combinations with x, x, y;  
	//			-generate all combinations with x, y, y;
	//		}	
	for(index1=values.length-1; index1>0; --index1)
	  for(index2=index1-1; index2>0; --index2){
		//-generate all combinations with x, x, y;
		comb=values[index1]+"."+values[index1]+"."+values[index2];
		combinationList.add(comb);

		comb=values[index1]+"."+values[index2]+"."+values[index1];
		combinationList.add(comb);

		comb=values[index2]+"."+values[index1]+"."+values[index1];
		combinationList.add(comb);			  
		//-generate all combinations with y, y, x;
		comb=values[index2]+"."+values[index2]+"."+values[index1];
		combinationList.add(comb);

		comb=values[index2]+"."+values[index1]+"."+values[index2];
		combinationList.add(comb);

		comb=values[index1]+"."+values[index2]+"."+values[index2];
		combinationList.add(comb);			  
	  }	


	//step (5)
	//	    5)VALORI UGUALI
	//	    for(x: da MaxX a 0) 
	//	      -generate the combination with x, x, x;
	for(index1=values.length-1; index1>=0; --index1){
	  //-generate the combination with x, x, x;
	  comb=values[index1]+"."+values[index1]+"."+values[index1];
	  combinationList.add(comb);
	}
	
	return combinationList.toArray(new String[1]);	
  }
  
  
  /**
   * Calculates all possible combinations of the given values on three positions, <br>
   * where order matters and duplicate values are possible.<br>
   * The order of the elements in the result list will try to put first the lements most different, <br>
   * by considering the combinations as RGB colors <br>
   * (this means that more 'pure' color comes first than less 'pure' ones).
   * 
   * @param values - the int[] containing all possible values
   * @return res - a int[][] containing all possible combinations with res[][0]=x, res[][1]=y, res[][2]=z
   */
  public static int[][] threePositionsCombinationsAsIntegers(int[] values){
	
//	int[] values={0, 127, 255};
//  int[] values={0, 85, 170, 255};
//  int[] values={0, 64, 128, 192, 255};
//  int[] values={0, 51, 102, 153, 204, 255};
	int index1=0, index2=0, index3=0; 
//	ArrayList<String> combinationList= new ArrayList<String>();
	int[][] combinations= new int[(int)Math.pow(values.length, 3)][];
	int[] comb=null;
	int combinationIndex=0;

	//	  PASSI PER CALCOLARE TUTTE LE COMBINAZIONI DI X VALORI SU TRE POSIZIONI:
	//		  [Dove MaxX=255 e MinX è il valore appena sopra 0]

	  
	//step (1)
	//	    1)VALORE SINGOLO
	//	    for(x: da MaxX a MinX){
	//	      -generate all combinations with x, 0, 0;
	//	      -generate all combinations with x, x, 0;
	//	    }
	for(index1=values.length-1; index1>0; --index1){
	  //-generate all combinations with x, 0, 0;
	  comb=new int[3];
	  comb[0]=values[index1]; comb[1]=0; comb[2]=0;
	  combinations[combinationIndex++]=comb;

	  comb=new int[3];
	  comb[0]=0; comb[1]=values[index1]; comb[2]=0;
	  combinations[combinationIndex++]=comb;

	  comb=new int[3];
	  comb[0]=0; comb[1]=0; comb[2]=values[index1];
	  combinations[combinationIndex++]=comb;

	  //-generate all combinations with x, x, 0;		
	  comb=new int[3];
	  comb[0]=values[index1]; comb[1]=values[index1]; comb[2]=0;
	  combinations[combinationIndex++]=comb;

	  comb=new int[3];
	  comb[0]=values[index1]; comb[1]=0; comb[2]=values[index1];
	  combinations[combinationIndex++]=comb;

	  comb=new int[3];
	  comb[0]=0; comb[1]=values[index1]; comb[2]=values[index1];
	  combinations[combinationIndex++]=comb;
	}

		
	//step (2)
	//	    2)DUE VALORI
	//      for(x: da MaxX a MinX)
	//	      for(y: dal valore sotto MaxX a MinX){
	//		    -generate all combinations with x, y, 0;
	//	      }
	for(index1=values.length-1; index1>0; --index1)
	  for(index2=index1-1; index2>0; --index2){
		//-generate all combinations with x, y, 0;
 	    comb=new int[3];
 	    comb[0]=values[index1]; comb[1]=values[index2]; comb[2]=0;
 	    combinations[combinationIndex++]=comb;

 	    comb=new int[3];
 	    comb[0]=values[index1]; comb[1]=0; comb[2]=values[index2];
 	    combinations[combinationIndex++]=comb;

 	    comb=new int[3];
 	    comb[0]=values[index2]; comb[1]=values[index1]; comb[2]=0;
 	    combinations[combinationIndex++]=comb;

 	    comb=new int[3];
 	    comb[0]=values[index2]; comb[1]=0; comb[2]=values[index1];
 	    combinations[combinationIndex++]=comb;

 	    comb=new int[3];
 	    comb[0]=0; comb[1]=values[index1]; comb[2]=values[index2];
 	    combinations[combinationIndex++]=comb;

 	    comb=new int[3];
 	    comb[0]=0; comb[1]=values[index2]; comb[2]=values[index1];
 	    combinations[combinationIndex++]=comb;
	  }
		
		
	//step (3)
	//		3)TRE VALORI TUTTI DIVERSI
	//		  for(x: da MaxX a MinX)
	//		    for(y: da MaxX a MinX){
	//		      for(z: da MaxX a MinX){
	//		        -generate all combinations with x, y, z;
	//		  }	
	for(index1=values.length-1; index1>0; --index1)
	  for(index2=index1-1; index2>0; --index2)
		for(index3=index2-1; index3>0; --index3){				
	  	  //-generate all combinations with x, y, z;
	 	  comb=new int[3];
	 	  comb[0]=values[index1]; comb[1]=values[index2]; comb[2]=values[index3];
	 	  combinations[combinationIndex++]=comb;

	 	  comb=new int[3];
	 	  comb[0]=values[index1]; comb[1]=values[index3]; comb[2]=values[index2];
	 	  combinations[combinationIndex++]=comb;

	 	  comb=new int[3];
	 	  comb[0]=values[index2]; comb[1]=values[index1]; comb[2]=values[index3];
	 	  combinations[combinationIndex++]=comb;

	 	  comb=new int[3];
	 	  comb[0]=values[index2]; comb[1]=values[index3]; comb[2]=values[index1];
	 	  combinations[combinationIndex++]=comb;

	 	  comb=new int[3];
	 	  comb[0]=values[index3]; comb[1]=values[index1]; comb[2]=values[index2];
	 	  combinations[combinationIndex++]=comb;

	 	  comb=new int[3];
	 	  comb[0]=values[index3]; comb[1]=values[index2]; comb[2]=values[index1];
	 	  combinations[combinationIndex++]=comb;
		}	


	//step (4)
	//	    4)TRE VALORI DI CUI DUE UGUALI E UNO DIVERSO
	//		for(x: da MaxX a MinX)
	//		  for(y: da MaxX a MinX){
	//		    -generate all combinations with x, x, y;  
	//			-generate all combinations with x, y, y;
	//		}	
	for(index1=values.length-1; index1>0; --index1)
	  for(index2=index1-1; index2>0; --index2){
		//-generate all combinations with x, x, y;
		comb=new int[3];
		comb[0]=values[index1]; comb[1]=values[index1]; comb[2]=values[index2];
		combinations[combinationIndex++]=comb;

		comb=new int[3];
		comb[0]=values[index1]; comb[1]=values[index2]; comb[2]=values[index1];
		combinations[combinationIndex++]=comb;

		comb=new int[3];
		comb[0]=values[index2]; comb[1]=values[index1]; comb[2]=values[index1];
		combinations[combinationIndex++]=comb;

		//-generate all combinations with y, y, x;
		comb=new int[3];
		comb[0]=values[index2]; comb[1]=values[index2]; comb[2]=values[index1];
		combinations[combinationIndex++]=comb;

		comb=new int[3];
		comb[0]=values[index2]; comb[1]=values[index1]; comb[2]=values[index2];
		combinations[combinationIndex++]=comb;

		comb=new int[3];
		comb[0]=values[index1]; comb[1]=values[index2]; comb[2]=values[index2];
		combinations[combinationIndex++]=comb;	  
	  }	


	//step (5)
	//	    5)VALORI UGUALI
	//	    for(x: da MaxX a 0) 
	//	      -generate the combination with x, x, x;
	for(index1=values.length-1; index1>=0; --index1){
	  //-generate the combination with x, x, x;
	  comb=new int[3];
	  comb[0]=values[index1]; comb[1]=values[index1]; comb[2]=values[index1];
	  combinations[combinationIndex++]=comb;	  
	}
	
	return combinations;	
  }
  
}
