/*
 * =========================================================================
 * cec09filter.cpp - select some nondominated pounsigned ints from a given population
 *
 * Copyright (c) 2008 Aimin Zhou
 * Dept. of Computing and Electronic Systems
 * Univ. of Essex
 * Colchester, CO4 0DY, U.K
 * azhou@essex.ac.uk
 * =========================================================================
 */

#include "mex.h"
#include <vector>
#include <cmath>
#include <iostream>

unsigned int	DIM, //dimension of F
				N,	 //number of data
				M;	 //number of selected data
std::vector< std::vector<double> > vF;	// column-wise popoulation 
std::vector< unsigned int > indS;  		// index of the selected points

// Dominate
// point 1 dominates point 2	: 1
// point 2 dominates point 1	: -1
// non-dominates each other	: 0
int Dominate(unsigned int iA, unsigned int iB)
{
	unsigned int strictBetter = 0,
				 strictWorse  = 0,
				 better		  = 0,
				 worse		  = 0,
				 i;

	for(i=0; i<DIM; i++)
	{
		if(vF[iA][i]<=vF[iB][i])
		{
			better++;
			strictBetter += vF[iA][i]<vF[iB][i]-1.0E-5 ? 1:0;
		}
		if(vF[iA][i]>=vF[iB][i])
		{
			worse++;
			strictWorse += vF[iA][i]>vF[iB][i]+1.0E-5 ? 1:0;
		}
	}

	if(better == DIM && strictBetter > 0) return 1;
	if(worse  == DIM && strictWorse  > 0) return -1;
	return 0;
}

void Filter()
{
	int dom;
	unsigned int i,j,k,nndom;
	std::vector<bool> domV(N);
	std::vector<unsigned int> nnindex(N);
	
	//step 1: find the dominated points
	for(i=0; i<N; i++) domV[i] = false;
	for(i=0; i<N; i++)
	{
		for(j=i+1; j<N; j++)
		{
			dom = Dominate(i,j);
			if(dom>0) 		domV[j] = true;
 			else if(dom<0) 	domV[i] = true;
 		}
	}
	nndom = 0;
	for(i=0; i<N; i++) if(!domV[i]) nnindex[nndom++] = i;
	
	//step 2: find the final solutions
	if(nndom<=M)
	{
		M    = nndom;
		indS.resize(nndom); 
		for(i=0; i<nndom; i++) indS[i] = nnindex[i];
	}
	else
	{
		indS.resize(M);
		std::vector<double> dis2set(nndom); for(i=0; i<nndom; i++) dis2set[i]  = 1.0E100;
		std::vector<bool> selected(nndom);  for(i=0; i<nndom; i++) selected[i] = false;
		
		// distance matrix
		std::vector< std::vector<double> > disV(nndom);
		for(i=0; i<nndom; i++) disV[i].resize(nndom);
		for(i=0; i<nndom; i++)
		{
			disV[i][i] = 0.0;
			for(j=i+1; j<nndom; j++)
			{
				disV[i][j] = 0.0;
				for(k=0; k<DIM; k++) disV[i][j] += (vF[nnindex[i]][k]-vF[nnindex[j]][k])*(vF[nnindex[i]][k]-vF[nnindex[j]][k]);
				disV[j][i] = disV[i][j];
			}
		}
		
		// select extreme points
		double fmin; unsigned int index;
		for(k=0; k<DIM; k++)
		{
			fmin = 1.0E100; index = 0;
			for(i=0; i<nndom; i++) if(!selected[i] && vF[nnindex[i]][k]<fmin) {fmin = vF[nnindex[i]][k]; index = i;}
			indS[k] = nnindex[index]; selected[index] = true;
			for(i=0; i<nndom; i++) if(!selected[i] && dis2set[i]>disV[i][index]) dis2set[i] = disV[i][index];
		}
		// select all the other points
		for(k=DIM; k<M; k++)
		{
			fmin = -1.0E100; index = 0;
			for(i=0; i<nndom; i++) if(!selected[i] && dis2set[i]>fmin) {fmin = dis2set[i]; index = i;}
			indS[k] = nnindex[index]; selected[index] = true;
			for(i=0; i<nndom; i++) if(!selected[i] && dis2set[i]>disV[i][index]) dis2set[i] = disV[i][index];
		}
	}	
}

void mexFunction(int nlhs, mxArray *plhs[], int nrhs,
                 const mxArray *prhs[])
{
	unsigned int i,j;
	// Check for proper number of arguments.
	if(nrhs != 2)
	{
		mexErrMsgTxt("Input data required: index = cec09filter(population, number).");
	} else if(nlhs != 1)
	{
		mexErrMsgTxt("One output argument: index = cec09filter(population, number).");
	}

	// Check for data number
	DIM 	= mxGetM(prhs[0]);	// dimension of objective vectors
	N		= mxGetN(prhs[0]);  // number of objective vectors
	M	    = (unsigned int)mxGetScalar(prhs[1]); // number of new vectors  

	// Copy data.
	double *vf = mxGetPr(prhs[0]);
	vF.resize(N);
	for(i=0; i<N; i++)
	{
		vF[i].resize(DIM); for(j=0; j<DIM; j++) vF[i][j] = vf[i*DIM+j];
	}

	// Select
	Filter();

	// Create matrix for the return arguments.
	plhs[0] = mxCreateDoubleMatrix(1,M, mxREAL);
	vf 		= mxGetPr(plhs[0]);
	// Copy data to return arguments.
	for(i=0; i<M; i++) vf[i] = indS[i]+1;
}
