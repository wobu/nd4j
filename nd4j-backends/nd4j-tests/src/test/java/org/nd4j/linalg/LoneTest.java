package org.nd4j.linalg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.SoftMax;
import org.nd4j.linalg.api.ops.impl.transforms.Tanh;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by agibsonccc on 4/1/16.
 */
@RunWith(Parameterized.class)
public class LoneTest extends BaseNd4jTest {
    public LoneTest(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testSoftmaxStability() {
        INDArray input = Nd4j.create(new double[]{ -0.75, 0.58, 0.42, 1.03, -0.61, 0.19, -0.37, -0.40, -1.42, -0.04}).transpose();
        System.out.println("Input transpose " + Shape.shapeToString(input.shapeInfo()));
        INDArray output = Nd4j.create(10,1);
        System.out.println("Element wise stride of output " + output.elementWiseStride());
        Nd4j.getExecutioner().exec(new SoftMax(input, output));
    }

    @Override
    public char ordering() {
        return 'c';
    }

    @Test
    public void testFlattenedView() {
        int rows = 8;
        int cols = 8;
        int dim2 = 4;
        int length = rows* cols;
        int length3d = rows * cols * dim2;

        INDArray first = Nd4j.linspace(1,length,length).reshape('c',rows,cols);
        INDArray second = Nd4j.create(new int[]{rows,cols},'f').assign(first);
        INDArray third = Nd4j.linspace(1,length3d,length3d).reshape('c',rows,cols,dim2);
        first.addi(0.1);
        second.addi(0.2);
        third.addi(0.3);

        first = first.get(NDArrayIndex.interval(4,8), NDArrayIndex.interval(0,2,8));
        for(int i = 0; i < first.tensorssAlongDimension(0); i++) {
            System.out.println(first.tensorAlongDimension(i,0));
        }

        for(int i = 0; i < first.tensorssAlongDimension(1); i++) {
            System.out.println(first.tensorAlongDimension(i,1));
        }
        second = second.get(NDArrayIndex.interval(3,7), NDArrayIndex.all());
        third = third.permute(0,2,1);

        INDArray cAssertion = Nd4j.create(new double[]{33.10, 35.10, 37.10, 39.10, 41.10, 43.10, 45.10, 47.10, 49.10, 51.10, 53.10, 55.10, 57.10, 59.10, 61.10, 63.10});
        INDArray fAssertion = Nd4j.create(new double[] {33.10, 41.10, 49.10, 57.10, 35.10, 43.10, 51.10, 59.10, 37.10, 45.10, 53.10, 61.10, 39.10, 47.10, 55.10, 63.10});
        assertEquals(cAssertion,Nd4j.toFlattened('c', first));
        assertEquals(fAssertion,Nd4j.toFlattened('f', first));
    }

    @Test
    public void testIndexingColVec() {
        int elements = 5;
        INDArray rowVector = Nd4j.linspace(1, elements, elements).reshape(1, elements);
        INDArray colVector = rowVector.transpose();
        int j;
        INDArray jj;
        for(int i = 0; i < elements; i++) {
            j = i+1;
            assertEquals(colVector.getRow(i).getInt(0), i+1);
            assertEquals(rowVector.getColumn(i).getInt(0), i+1);
            assertEquals(rowVector.get(NDArrayIndex.interval(i, j)).getInt(0),i+1);
            assertEquals(colVector.get(NDArrayIndex.interval(i, j)).getInt(0),i+1);
            System.out.println("Making sure index interval will not crash with begin/end vals...");
            jj = colVector.get(NDArrayIndex.interval(i,i+10));
            jj = colVector.get(NDArrayIndex.interval(i,i+10));
        }
    }

    @Test
    public void concatScalarVectorIssue() {
        //A bug was found when the first array that concat sees is a scalar and the rest vectors + scalars
        INDArray arr1 = Nd4j.create(1,1);
        INDArray arr2 = Nd4j.create(1,8);
        INDArray arr3 = Nd4j.create(1,1);
        INDArray arr4 = Nd4j.concat(1,arr1,arr2,arr3);
        assertTrue(arr4.sumNumber().floatValue() <= Nd4j.EPS_THRESHOLD);
    }

    @Test
    public void reshapeTensorMmul() {
        INDArray a = Nd4j.linspace(1, 2, 12).reshape(2, 3, 2);
        INDArray b = Nd4j.linspace(3, 4, 4).reshape(2, 2);
        int[][] axes=new int[2][];
        axes[0]=new int[]{0,1};
        axes[1]=new int[]{0,2};

        //this was throwing an exception
        INDArray c = Nd4j.tensorMmul(b, a, axes);
    }

    @Test
    public void maskWhenMerge() {
        DataSet dsA = new DataSet(Nd4j.linspace(1, 15, 15).reshape(1, 3, 5), Nd4j.zeros(1, 3, 5));
        DataSet dsB = new DataSet(Nd4j.linspace(1, 9, 9).reshape(1, 3, 3), Nd4j.zeros(1, 3, 3));
        List<DataSet> dataSetList = new ArrayList<DataSet>();
        dataSetList.add(dsA);
        dataSetList.add(dsB);
        DataSet fullDataSet = DataSet.merge(dataSetList);
        assertTrue(fullDataSet.getFeaturesMaskArray() != null);

        DataSet fullDataSetCopy = fullDataSet.copy();
        assertTrue(fullDataSetCopy.getFeaturesMaskArray() != null);

    }

    @Test
    public void testRelu() {
        INDArray aA = Nd4j.linspace(-3,4,8).reshape(2,4);
        INDArray aD = Nd4j.linspace(-3,4,8).reshape(2,4);
        INDArray b = Nd4j.getExecutioner().execAndReturn(new Tanh(aA));
        //Nd4j.getExecutioner().execAndReturn(new TanhDerivative(aD));
        System.out.println(aA);
        System.out.println(aD);
        System.out.println(b);
    }

    @Test
    public void permuteiTest() {
        INDArray A = Nd4j.linspace(1,16,16).reshape(4,2,2);
        INDArray B = A.dup();

        int[] reArrange = new int[] {2,0,1};

        A = A.permute(reArrange);
        B = B.permutei(reArrange);

        assertTrue(A.equals(B));

        int[] newShape = new int[] {4,4};
        A = A.reshape(newShape);
        B = B.reshape(newShape);

        assertTrue(A.equals(B));
    }

}
