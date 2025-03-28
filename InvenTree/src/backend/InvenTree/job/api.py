from django.urls import include, path, re_path
from rest_framework import permissions, serializers, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.viewsets import ModelViewSet, ViewSet
from rest_framework.decorators import action
from .models import Job, JobItem, JobTech, Address
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from stock.models import StockItem
from .serializers import JobSerializer, JobItemSerializer, JobItemListSerializer, JobTechSerializer, AddressSerializer

class JobViewSet(ModelViewSet):
    """
    API endpoint that allows jobs to be viewed or edited.
    By default, only fetch jobs where the authenticated user is assigned as a technician.
    """
    serializer_class = JobSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        """
        Filter jobs to only include those where the authenticated user is assigned as a technician.
        """
        user = self.request.user
        return Job.objects.filter(techs__technician=user)

class JobItemViewSet(ModelViewSet):
    """
    API endpoint that allows job items to be viewed or edited.
    """
    queryset = JobItem.objects.all()
    serializer_class = JobItemSerializer
    permission_classes = [permissions.IsAuthenticated]



class JobDetail(APIView):
    """
    Retrieve, update or delete a job instance.
    """
    queryset = Job.objects.all()  # Ensure this line is present

    def get(self, request, pk, format=None):
        job = Job.objects.get(pk=pk)
        serializer = JobSerializer(job)
        return Response(serializer.data)

    def put(self, request, pk, format=None):
        job = Job.objects.get(pk=pk)
        serializer = JobSerializer(job, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        job = Job.objects.get(pk=pk)
        job.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
    
    def update(self, request, pk, format=None):
        job = Job.objects.get(pk=pk)
        serializer = JobSerializer(job, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class JobIndex(APIView):
    """
    List all jobs, or create a new job.
    """
    queryset = Job.objects.all()  # Ensure this line is present

    def get(self, request, format=None):
        jobs = Job.objects.all()
        serializer = JobSerializer(jobs, many=True)
        return Response(serializer.data)

    def post(self, request, format=None):
        serializer = JobSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class AddressViewSet(ModelViewSet):
    """
    API endpoint that allows addresses to be viewed or edited.
    """
    queryset = Address.objects.all()
    serializer_class = AddressSerializer
    permission_classes = [permissions.IsAuthenticated]


class JobItemsForJobViewSet(ModelViewSet):
    """
    A viewset for viewing and editing job items for a specific job.
    """
    serializer_class = JobItemSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        job_id = self.kwargs['pk']
        return JobItem.objects.filter(job_id=job_id)

    @action(detail=True, methods=['put', 'patch'])
    def update_items(self, request, pk=None):
        job_items = self.get_queryset()
        serializer = JobItemListSerializer(instance=job_items, data=request.data, many=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=True, methods=['delete'])
    def delete_items(self, request, pk=None):
        job_items = self.get_queryset()
        job_items.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

class JobItemsForJob(APIView):
    """
    Retrieve all job items for a specific job.
    """

    queryset = JobItem.objects.all()
    def get(self, request, pk, format=None):
        job_items = JobItem.objects.filter(job_id=pk)
        serializer = JobItemSerializer(job_items, many=True)
        return Response(serializer.data)
    
    def put(self, request, pk, format=None):
        job_items = JobItem.objects.filter(job_id=pk)
        serializer = JobItemSerializer(job_items, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    def delete(self, request, pk, format=None):
        job_items = JobItem.objects.filter(job_id=pk)
        job_items.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
    
    def update(self, request, pk, format=None):
        job_items = JobItem.objects.filter(job_id=pk)
        serializer = JobItemSerializer(job_items, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class AddressDetail(APIView):
    """
    Retrieve, update or delete an address instance.
    """
    def get(self, request, pk, format=None):
        address = Address.objects.get(pk=pk)
        serializer = AddressSerializer(address)
        return Response(serializer.data)

    def put(self, request, pk, format=None):
        address = Address.objects.get(pk=pk)
        serializer = AddressSerializer(address, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        address = Address.objects.get(pk=pk)
        address.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
    
    def update(self, request, pk, format=None):
        address = Address.objects.get(pk=pk)
        serializer = AddressSerializer(address, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class JobAddressDetail(APIView):
    """
    Retrieve, update, or delete the address for a specific job.
    """
    def get_queryset(self, pk):
        """
        By default, only fetch jobs which have an address assigned.
        """
        return Job.objects.filter(Address=pk)

    def get(self, request, pk, format=None):
        """
        Retrieve the address for the specified job.
        """
        job = Job.objects.get(pk=pk)
        address = job.address
        serializer = AddressSerializer(address)
        return Response(serializer.data)

    def put(self, request, pk, format=None):
        """
        Update the address for the specified job.
        """
        job = Job.objects.get(pk=pk)
        address = job.address
        serializer = AddressSerializer(address, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        """
        Delete the address for the specified job.
        """
        job = Job.objects.get(pk=pk)
        address = job.address
        address.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

class JobTechViewSet(ModelViewSet):
    """
    API endpoint for managing JobTech assignments.
    Allows technicians to create or change the status of a job.
    """
    serializer_class = JobTechSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        """
        By default, only fetch jobs assigned to the authenticated technician.
        """
        user = self.request.user
        return JobTech.objects.filter(technician=user)

    def perform_create(self, serializer):
        """
        Automatically set the technician to the authenticated user when creating a JobTech entry.
        """
        serializer.save(technician=self.request.user)

class JobTechViewSet(ModelViewSet):
    """
    API endpoint for managing JobTech assignments.
    Allows technicians to create or change the status of a job.
    """
    serializer_class = JobTechSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        """
        By default, only fetch jobs assigned to the authenticated technician.
        """
        user = self.request.user
        return JobTech.objects.filter(technician=user)

    def perform_create(self, serializer):
        """
        Automatically set the technician to the authenticated user when creating a JobTech entry.
        """
        serializer.save(technician=self.request.user)        

class JobTechForJobViewSet(ModelViewSet):
    """
    A viewset for viewing and managing JobTech entries for a specific job.
    """
    serializer_class = JobTechSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        """
        Filter JobTech entries to only include those for the specified job.
        """
        job_id = self.kwargs['pk']
        return JobTech.objects.filter(job_id=job_id)

    def perform_create(self, serializer):
        """
        Automatically associate the JobTech entry with the specified job.
        """
        job_id = self.kwargs['pk']
        serializer.save(job_id=job_id, technician=self.request.user)


class PartJobTransfer(ViewSet):
    """
    API endpoint for transferring parts from a StockItem's location to a job.
    """
    permission_classes = [permissions.IsAuthenticated]
    queryset = JobItem.objects.none()

    def create(self, request, pk=None):
        """
        Transfer parts from a StockItem's location to a job.
        """
        # Extract data from the request
        stock_item_id = request.data.get('stock_item')
        job_id = request.data.get('job')
        quantity = request.data.get('quantity')

        # Validate the input data
        if not all([stock_item_id, job_id, quantity]):
            return Response({'error': 'All fields (stock_item, job, quantity) are required.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            stock_item = StockItem.objects.get(pk=stock_item_id)
            job = Job.objects.get(pk=job_id)
        except StockItem.DoesNotExist:
            return Response({'error': 'StockItem not found.'}, status=status.HTTP_404_NOT_FOUND)
        except Job.DoesNotExist:
            return Response({'error': 'Job not found.'}, status=status.HTTP_404_NOT_FOUND)

        # Check if the StockItem has enough quantity
        if stock_item.quantity < quantity:
            return Response({'error': 'Not enough quantity in the StockItem.'}, status=status.HTTP_400_BAD_REQUEST)

        # Deduct the quantity from the StockItem
        stock_item.quantity -= quantity
        stock_item.save()

        # Add the quantity to the job
        job_item, created = JobItem.objects.get_or_create(job=job, stock_item=stock_item.part, defaults={'quantity': 0})
        job_item.quantity += quantity
        job_item.save()

        return Response({'message': 'Part transferred successfully.'}, status=status.HTTP_200_OK)
    


# URL patterns for the job API
job_api_urls = [
    path(
        '',
        include([
            path('<int:pk>/', include([
                path('', JobDetail.as_view(), name='api-job-detail'),
                path('items/', 
                     include([
                         path('', JobItemsForJobViewSet.as_view({'get': 'list', 'put': 'update_items', 'delete': 'delete_items'}), name='api-job-items-for-job'),
                         path('transfer/', PartJobTransfer.as_view({'post': 'create'}), name='api-job-transfer'),
                     ])),
                
                path('tech/', JobTechForJobViewSet.as_view({'get': 'list', 'post': 'create'}), name='api-job-tech-for-job'),
                path('address/', JobAddressDetail.as_view(), name='api-job-address'),
            ])),
            path('', JobIndex.as_view(), name='api-job-list'),
        ]),
    ),
]