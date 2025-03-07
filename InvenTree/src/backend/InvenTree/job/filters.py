import django_filters
from .models import Job, Address, JobItem

class JobFilter(django_filters.FilterSet):
    """Filter for Job model."""
    name = django_filters.CharFilter(lookup_expr='icontains')
    address__city = django_filters.CharFilter(lookup_expr='icontains')
    address__state = django_filters.CharFilter(lookup_expr='icontains')
    address__country = django_filters.CharFilter(lookup_expr='icontains')

    class Meta:
        model = Job
        fields = ['name', 'address__city', 'address__state', 'address__country']

class AddressFilter(django_filters.FilterSet):
    """Filter for Address model."""
    street = django_filters.CharFilter(lookup_expr='icontains')
    city = django_filters.CharFilter(lookup_expr='icontains')
    state = django_filters.CharFilter(lookup_expr='icontains')
    zip_code = django_filters.CharFilter(lookup_expr='icontains')
    country = django_filters.CharFilter(lookup_expr='icontains')

    class Meta:
        model = Address
        fields = ['street', 'city', 'state', 'zip_code', 'country']

class JobItemFilter(django_filters.FilterSet):
    """Filter for JobItem model."""
    job__name = django_filters.CharFilter(lookup_expr='icontains')
    stock_item__name = django_filters.CharFilter(lookup_expr='icontains')
    quantity = django_filters.NumberFilter()

    class Meta:
        model = JobItem
        fields = ['job__name', 'stock_item__name', 'quantity']