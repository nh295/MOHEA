 /*
 * =============================================================
 * Upsilon.c
 *
 * Calculate the distance from one set to another set
 *
 * Aimin ZHOU
 * azhou@essex.ac.uk
 * Copyright (c) 2005 Dept. of Computer Science, Uinv. of Essex
 * =============================================================
 */

#include "mex.h"
#include "math.h"

double Upsilon( double* S, double* Q, int row, int Scol, int Qcol )
{
	int i,j,k;
	double d,min,dis;
	dis=0.0;
	for( i=0; i<Scol; i++ )
	{
		min = 1.0E200;
		for( j=0; j<Qcol; j++ )
		{
			d=0.0;
			for( k=0; k<row; k++ )
				d += ( S[i*row+k] - Q[j*row+k] ) * ( S[i*row+k] - Q[j*row+k] );
			if( d < min ) min = d;
		}
		dis += sqrt(min);
	}
	return dis/(double)( Scol + 0.0 );
}


/* The gateway routine */
void mexFunction(int nlhs, mxArray *plhs[],
                 int nrhs, const mxArray *prhs[])
{
  double *S, *Q;
  double dis;
  int status,Srows,Scols,Qrows,Qcols;

  /*  Check for proper number of arguments. */
  if (nrhs != 2) mexErrMsgTxt("Two inputs required.");
  if (nlhs != 1) mexErrMsgTxt("One output required.");

  /* Create pointers to the input matrix S, Q. */
  S = mxGetPr(prhs[0]);
  Q = mxGetPr(prhs[1]);

  /* Get the dimensions of the matrix input */
  Srows = mxGetM(prhs[0]);
  Scols = mxGetN(prhs[0]);
  Qrows = mxGetM(prhs[1]);
  Qcols = mxGetN(prhs[1]);

  /* Check for dimension of input matrix. */
  if (Srows != Qrows) mexErrMsgTxt("Input matrix should have same rows.");

  /* Calculate the upsilon measure */
  plhs[0] = mxCreateDoubleMatrix(1,1, mxREAL);
  *(mxGetPr(plhs[0])) = Upsilon( S, Q, Srows, Scols, Qcols );
}