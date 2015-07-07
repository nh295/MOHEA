function IGD = computeIGD(set,refset)
%computes the inverted generational distance for a particular approximation
%set to the true pareto front.

[nPFpts, ~] = size(refset);

IGD = 0;
for i=1:nPFpts
    IGD = IGD + min(pdist2(set,refset(i,:),'euclidean'));
end

IGD = IGD / nPFpts;