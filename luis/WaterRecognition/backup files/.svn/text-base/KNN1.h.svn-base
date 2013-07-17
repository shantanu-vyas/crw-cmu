/* 
 * File:   KNN.h
 * Author: pototo
 *
 * Created on July 21, 2011, 11:12 AM
 */

#ifndef POTOTO_NEIGHBOR_H
#define	POTOTO_NEIGHBOR_H

#include <cstdlib>
#include <math.h>
#include <string.h>
#include <iostream>

#include "highgui.h"
#include "ml.h"
#include "cv.h"

//creates a k-nearest neighbor class based on Euclidean distances
// trainingMatrix - 
class KNN
{
public:
    KNN();
    KNN(CvMat* trainingMatrix, CvMat* sampleMatrix, int K);
    
    //get functions
    CvMat* getEucDistance() const;      //get the Euclidean distance
private:
    int m_k;            //(1x1) - number of nearest neighbors desired
    CvMat* m_trainingMat;   //(N x D)- N vectors with dimensionality D (within which we search for the nearest neighbors)
    CvMat* m_sampleMat;     //(M x D)- M query vectors with dimensionality D
    CvMat* m_neighborDistance;      //returns the distance to the sample. Distance store in ascending order
    
};

#endif	/* POTOTO_NEIGHBOR_H */