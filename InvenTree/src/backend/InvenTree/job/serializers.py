from rest_framework import serializers
from .models import Job, Address, JobItem

class AddressSerializer(serializers.ModelSerializer):
    """Serializer for Address model."""

    class Meta:
        model = Address
        fields = ['street', 'city', 'state', 'zip_code', 'country']

class JobSerializer(serializers.ModelSerializer):
    """Serializer for Job model."""
    address = AddressSerializer()

    class Meta:
        model = Job
        fields = ['job_id', 'name', 'address']

class JobItemSerializer(serializers.ModelSerializer):
    """Serializer for JobItem model."""
    job = JobSerializer()
    stock_item = serializers.StringRelatedField()

    class Meta:
        model = JobItem
        fields = ['job', 'stock_item', 'quantity']