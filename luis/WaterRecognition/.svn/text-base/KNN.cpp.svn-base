#include "KNN.h"

KNN::KNN(CvMat* trainingMatrix, CvMat* sampleMatrix, int K)
{
    m_trainingMat = cvCreateMat(K,K,CV_32FC1);
    m_sampleMat = cvCreateMat(K,K,CV_32FC1);
    m_neighborDistance = cvCreateMat(K,K,CV_32FC1);;
    m_trainingMat = trainingMatrix;
    m_sampleMat = sampleMatrix;
    m_k = K;
}

CvMat* KNN::getEucDistance() const
{
    double sum = 0.0;    
    
    for (int i = 0; i < m_k; i++)
    {
        for (int j = 0;j < m_k; j++ )
        {
            sum = sum + pow(fabs(cvmGet(m_trainingMat,i,j) - cvmGet(m_sampleMat,i,j)), 2);
            cvmSet(m_neighborDistance,i,j,sqrt(sum));
        } 
    }
    cvSort(m_neighborDistance,m_neighborDistance,CV_SORT_ASCENDING);
    return m_neighborDistance;
}
