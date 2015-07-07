function HV = computeHV(set,refpoint,option)

%This function computes the hypervolume of a approximate set with respect
%to a specified reference point. This function can compute the hypervolume
%for either a maximization problem or a minimization problem. The default
%is set to minimization problem, but the option can be set to maximization.
%The approximate set is an n x m where n is the number of solutions in the
%approximate set and m is the number of objectives. It is assumed that the
%approximation set does not contain any dominated points
% 
%for minimization problem
%   HV = computeHV(set,'min')
%
%for minimization problem
%   HV = computeHV(set,'max')


%check minimization or maximization problem
[nPts, dim] = size(set);
if length(refpoint)~=dim
    error('Dimension mismatch between reference point and approximate set')
end

%get rid of points that are dominated by reference point
getRid = false(nPts,1);
if isempty(option) || strcmp('min',option)
    for i=1:nPts
        for j=1:dim
            if set(i,j)>refpoint(j)
                getRid(i) = true;
            else
                set(i,j) = refpoint(j) - set(i,j);
            end
        end
    end
elseif  strcmp('max',option)
    for i=1:nPts
        for j=1:dim
            if set(i,j)<refpoint(j)
                getRid(i) = true;
            else
                set(i,j) = set(i,j) - refpoint(j);
            end
        end
    end
else
    error('Invalid option selected');
end
set = set(~getRid,:);
[remainingPts,~] = size(set);

%sort the objectives in one dimension
[~,I] = sort(set(:,1));
sortedSet = set(I,:);
dx = [sortedSet(1,1)-0;diff(sortedSet(:,1))];
HV = 0;

for i=1:remainingPts
    HV = HV + dx(i)*sortedSet(i,2);
end